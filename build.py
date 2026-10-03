"""Build unsigned APKs using locally installed Android SDK tools and JDK 17."""
from pathlib import Path
import os
import shutil
import subprocess
import zipfile

root = Path(__file__).resolve().parent
sdk = Path(os.environ['ANDROID_SDK_ROOT'])
java_home = Path(os.environ['JAVA_HOME'])
tools = sdk / 'build-tools' / '34.0.0'
android = sdk / 'platforms' / 'android-30' / 'android.jar'
suffix = '.exe' if os.name == 'nt' else ''
java = java_home / 'bin' / ('java' + suffix)
javac = java_home / 'bin' / ('javac' + suffix)

def run(*args):
    subprocess.run([str(a) for a in args], check=True)

for project_name, artifact in [('app', 'APK-Installer'), ('installation-test', 'Installation-Test')]:
    project = root / project_name
    build = root / 'build' / project_name
    if build.exists():
        shutil.rmtree(build)
    classes = build / 'classes'
    dex = build / 'dex'
    classes.mkdir(parents=True)
    dex.mkdir()
    linked = build / 'resources.apk'
    args = [tools / ('aapt2' + suffix), 'link', '-I', android,
            '--manifest', project / 'AndroidManifest.xml', '-o', linked]
    if (project / 'res').exists():
        resources = build / 'compiled-resources.zip'
        run(tools / ('aapt2' + suffix), 'compile', '--dir', project / 'res', '-o', resources)
        args.append(resources)
    run(*args)
    run(javac, '--release', '8', '-encoding', 'UTF-8', '-classpath', android,
        '-d', classes, *sorted((project / 'src').rglob('*.java')))
    run(java, '-cp', tools / 'lib' / 'd8.jar', 'com.android.tools.r8.D8',
        '--min-api', '28', '--lib', android, '--output', dex, *sorted(classes.rglob('*.class')))
    unsigned = build / 'unsigned.apk'
    with zipfile.ZipFile(linked) as source, zipfile.ZipFile(unsigned, 'w') as dest:
        for entry in source.infolist():
            dest.writestr(entry, source.read(entry.filename))
        dest.write(dex / 'classes.dex', 'classes.dex', compress_type=zipfile.ZIP_DEFLATED)
    dist = root / 'dist'
    dist.mkdir(exist_ok=True)
    run(tools / ('zipalign' + suffix), '-f', '4', unsigned, dist / (artifact + '-unsigned.apk'))
    print('Built', dist / (artifact + '-unsigned.apk'))
