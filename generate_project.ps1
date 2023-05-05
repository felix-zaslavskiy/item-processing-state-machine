Write-Output "concatenating"
Start-Process -FilePath "python" -ArgumentList "..\useful-python-scripts\concat_java_files.py", `
 ".\src\main\java\com\hexadevlabs\simplefsm\ProcessingData.java", `
 ".\src\main\java\com\hexadevlabs\simplefsm\ProcessingStep.java", `
 ".\src\main\java\com\hexadevlabs\simplefsm\State.java", `
 ".\src\main\java\com\hexadevlabs\simplefsm\FSMState.java", `
 ".\src\main\java\com\hexadevlabs\simplefsm\NamedEntity.java", `
 ".\src\main\java\com\hexadevlabs\simplefsm\ExceptionInfo.java", `
 ".\src\main\java\com\hexadevlabs\simplefsm\NFSM.java"