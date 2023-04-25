Write-Output "concatenating"
Start-Process -FilePath "python" -ArgumentList "..\useful-python-scripts\concat_java_files.py", `
 ".\src\main\java\simplefsm\ProcessingData.java", `
 ".\src\main\java\simplefsm\ProcessingStep.java", `
 ".\src\main\java\simplefsm\State.java", `
 ".\src\main\java\simplefsm\FSMState.java", `
 ".\src\main\java\simplefsm\NamedEntity.java", `
 ".\src\main\java\simplefsm\ExceptionInfo.java", `
 ".\src\main\java\simplefsm\NFSM.java"
# ".\src\main\java\demo\SimpleFSMDemo.java"