#!/usr/bin/env python3
"""Compile FSR's precision/gather variants; requires glslangValidator and spirv-dis.

NV_gpu_shader5 must be tested on an NVIDIA driver: glslang does not support it.
The EXT and AMD variants exercise the same half-float compatibility helpers.
"""
from pathlib import Path
import re
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[3] / "src/main/resources/assets/renderscale/shaders"


def expand_imports(source):
    def include(match):
        if match[1] == "minecraft:globals.glsl":
            return "layout(std140, binding=0) uniform Globals { vec2 ScreenSize; };"
        included = (ROOT / "include" / match[1].split(":")[1]).read_text()
        # RenderPearl rewrites moj_import and shader interfaces only in the root source.
        assert "#moj_import" not in included, match[1]
        assert "uniform sampler" not in included and "out vec4 fragColor" not in included, match[1]
        return included

    return re.sub(r"#moj_import <([^>]+)>", include, source)


def main():
    for tool in ("glslangValidator", "spirv-dis"):
        if not shutil.which(tool):
            raise SystemExit(f"Missing required tool: {tool}")
    with tempfile.TemporaryDirectory(prefix="renderscale-fsr-") as directory:
        for shader in ("easu", "rcas"):
            for precision, extension in (("fp32", 0), ("fp16-ext", 1), ("fp16-amd", 2)):
                for explicit_gather in (False, True):
                    label = f"{shader}-{precision}-{'explicit' if explicit_gather else 'native'}"
                    suffix = "_fp16" if extension else ""
                    source = expand_imports((ROOT / "core" / f"{shader}{suffix}.fsh").read_text())
                    defines = f"\n#define RENDERSCALE_FP16 {extension}" if extension else ""
                    if explicit_gather:
                        defines += "\n#define RENDERSCALE_EXPLICIT_GATHER"
                    version, body = source.split("\n", 1)
                    source = version + defines + "\n" + body
                    path = Path(directory) / f"{label}.frag"
                    binary = path.with_suffix(".spv")
                    path.write_text(source)
                    subprocess.run([
                        "glslangValidator", "-G", "--auto-map-bindings", "--auto-map-locations",
                        "-o", str(binary), str(path),
                    ], check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
                    assembly = subprocess.check_output(["spirv-dis", str(binary)], text=True)
                    # Check that FP16 really generates half arithmetic, with no int16 or
                    # storage feature requirement, and that fallback needs no half support.
                    assert ("OpTypeFloat 16" in assembly) == bool(extension), label
                    assert "OpCapability Int16" not in assembly, label
                    assert "OpCapability Storage" not in assembly, label
                    print(f"PASS {label}")


if __name__ == "__main__":
    try:
        main()
    except subprocess.CalledProcessError as error:
        raise SystemExit(error.stdout)
