# CommandRunner - 本地执行系统命令

在已开启USB调试的手机上，直接执行系统级shell命令，无需root。

## 用途
通过USB调试授权，执行需要更高权限的系统命令。

## 命令列表（按顺序执行）
1. `cmd appops set com.sting.virtualloc android:mock_location allow`
2. `settings put global mock_location_enforced 0`
3. `settings put secure mock_location_app com.sting.virtualloc`
4. `settings put global development_settings_enabled 0`

## 操作流程
1. 打开app
2. 点击"执行第1步"
3. 等待完成，点击"执行第2步"
4. 依此类推
5. 最后点击"重启手机"（强制重启）

## 重要提醒
执行重启后，需要用户**手动长按电源键**重启手机。