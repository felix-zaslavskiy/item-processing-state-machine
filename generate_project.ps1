Write-Output "concatenating"
Start-Process -FilePath "python" -ArgumentList "..\useful-python-scripts\concat_java_files.py", `
 ".\src\main\java\nfsm\ProcessingData.java", `
 ".\src\main\java\nfsm\ProcessingStep.java", `
 ".\src\main\java\nfsm\State.java", `
 ".\src\main\java\nfsm\NFSM.java", `
 ".\src\main\java\demo\NFSMDemo.java"