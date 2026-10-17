"""Compile unchanged Java sources and capture a bounded partial simulation."""
from pathlib import Path
import subprocess
import re
import tempfile

ROOT=Path(__file__).resolve().parents[1]
settings=subprocess.run(['java','-XshowSettings:properties','-version'],capture_output=True,text=True,timeout=20)
JDK=Path(re.search(r'java.home = (.+)',settings.stderr).group(1).strip())
with tempfile.TemporaryDirectory() as folder:
    sources=sorted((ROOT/'src/main/java').glob('*.java'))
    build=subprocess.run([str(JDK/'bin/javac.exe'),'--release','8','-encoding','UTF-8','-d',folder]+[str(p) for p in sources],capture_output=True,text=True,check=True,timeout=60)
    print('SwiftCart | Windows | JDK 23.0.2, --release 8 | 2026-10-17')
    print('Compile: javac --release 8 -encoding UTF-8 -d <temp> src/main/java/*.java')
    print('Compiled',len(sources),'source files successfully.')
    print('Run: java -cp <temp> SwiftCartMain (45-second observation window)')
    process=subprocess.Popen([str(JDK/'bin/java.exe'),'-cp',folder,'SwiftCartMain'],cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True)
    try:
        output,errors=process.communicate(timeout=45)
        print('Process exited naturally:',process.returncode)
    except subprocess.TimeoutExpired:
        process.kill()
        output,errors=process.communicate()
        print('Stopped after 45 seconds. Partial run; normal five-minute shutdown not tested.')
    if errors.strip(): raise RuntimeError(errors)
    lines=output.splitlines()
    print('\nActual event excerpts (first matching lines, original order within each stage):')
    for prefix in ['OrderIntake:', 'Picker-', 'Picker:', 'Packer:', 'Labeller:', 'Sorter:', 'Loader:', 'Truck', 'AGVFailure']:
        selected=[line for line in lines if line.startswith(prefix)]
        if selected:
            print('\n'+prefix)
            print('\n'.join(selected[:3]))
    print('\nObserved event counts (log lines, not unique-order correctness assertions):')
    for label,token in [('Intake accepted','accepted (Thread: OrderThread'),('Sorted','sorted to'),('Containers created','sealed and sent to loading queue'),('Truck dispatch lines','dispatched')]:
        print(label+': '+str(sum(token in line for line in lines)))
    assert 'OrderIntake:' in output and 'Sorter:' in output and 'Packer:' in output
    print('Smoke scope: compilation and active pipeline stages observed; no full-run totals claimed.')
