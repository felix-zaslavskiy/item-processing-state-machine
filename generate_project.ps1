Write-Output "concatenating"
Start-Process -FilePath "python" -ArgumentList "..\useful-python-scripts\concat_java_files.py", `
 "nfsm\ProcessingData.java", `
 "nfsm\ProcessingStep.java", `
 "nfsm\State.java", `
 "nfsm\FSMState.java", `
 "nfsm\NFSM.java", `
 "demo\NFSMDemo.java"