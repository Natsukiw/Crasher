**发布帖：**[https://www.mcbbs.net/thread-1485785-1-1.html](_https://www.mcbbs.net/thread-1485785-1-1.html_)

### **Crasher**
写这个单纯自用，但是感觉写出来了也分享一下


### 插件功能：

### 取消数据包包处理和发送：
灵感来自"ShadowBan"插件的"ShadowKick"功能，但是没有存储数据包重新发送功能和放行心跳包功能
字面意思，取消服务端的数据包处理和发送能力，心跳包(PacketPlayInKeepAlive PacketPlayOutKeepAlive)除外
且支持存储数据包进行一定时间后一起处理和发送能力(准备开发记录时间获得时间差进行发送)，通俗一点就是比如你玩国外服务器，伤害可能是你坠落后一会才会扣血，发文字一会才会显示，就像网络延迟一样，但这个是可控的！

### 爆炸崩端：
灵感来自"SuperBan"插件，但是必须依赖于"ProtocolLib"插件且部分作弊客户端会屏蔽爆炸包无法崩掉
原理服务端异步往客户端发送自定义数量MAX_VAULE数值爆炸包，包含Vec3D，由于还没渲染客户端就开始计算所以根本看不见爆炸效果，玩家几乎无察觉
秒炸端，几乎不消耗服务器性能，但是崩不掉部分作弊客户端

### 实体崩端：
这个开发出来主要应对上述部分作弊客户端无法崩的问题
原理服务端异步往客户端发送大量(可自定义)实体数据包，也可选择发送少量达到降低FPS目的(实际上就这一段距离而已，客户端看到才会渲染)，实体为特殊实体且经过隐藏，玩家几乎无察觉
在服务器性能和网络足够的情况下也是秒炸端，但是会消耗较多(好吧其实也不是很多)带宽进行发包

### 冻结：
这个开发出来主要应对作弊玩家
原理是打开Inventory/GUI的时候玩家无法移动，冻结的时候玩家无法移动/传送/受到伤害，关闭GUI会自动开启新的，部分注入客户端无法调出菜单自毁，若在冻结中退出可以直接自定义命令封禁
几乎不影响服务器性能


### 下载地址：
[https://github.com/xuexu2/Crasher/releases](_https://github.com/xuexu2/Crasher/releases_)

理论用反射兼容大部分版本，请实测，有问题请发Issue，欢迎PR


### 权限(permissions):
OP权限：crasher.admin

### 命令(commands):
/Crasher <Player/reload> <cancel_packets/explosions/entitys>


### 配置文件(config.yml)：
```
# C R A S H E R
# The code written is generally for personal use~
# English is not very good, many of them are machine translated, sorry
# 写的代码一般，就是拿来自用的~
# 英文不是很好，很多都是机翻，不好意思

# Settings
# 设置
Settings:
  # Automatically check for updates and remind
  # 自动检查更新并提醒
  CheckUpdate: true
  # Cancel all switch settings for sending and receiving data packets
  # 取消所有数据包发送和接受的开关设置
  CancelPackets:
    # Whether to kick out the player after canceling all packets sent using the packetsCancel command (kicked out packets are only visible to the server). Default: true
    # 使用packetscancel命令后取消所有数据包发送后是否踢出玩家（踢出数据包只有服务端可见）。默认: true
    CancelPacketsKick: true
    #[Warning] This feature may have security and performance issues if it has not been fully tested!!! The core of plugin construction is 1.8_R3, if the server version and plugin version do not match, there may be errors or major vulnerabilities (you can choose to enable it if you don't care). Save the received and sent packets and forge a packet for the server to reprocess and send after re entering the command. Default: false
    # [警告] 这个功能未经过完整测试可能会有安全和性能问题！！！插件构建核心是1.8_R3，若服务端版本和插件版本不符可能会出现报错或重大漏洞（不在乎可以选择开启）保存接受和发送的数据包并在重新输入命令后给服务端伪造一份数据包重新处理以及发送。默认: false
    CancelPacketsResend: false
  # Entity packet crash settings
  # 实体数据包崩溃设置
  EntitysCrash:
    #Whether to kick out the player after canceling all packets sent using the entitys command (kicked out packets are only visible to the server). Default: false
    # 使用entitys命令后取消所有数据包发送后是否踢出玩家（踢出数据包只有服务端可见）。默认: false
    CancelKick: false
    # Number of packets sent. Default: 99999
    # 发送的数据包数量。默认：99999
    PacketLimit: 99999
  # Explosive packet crash settings
  # 爆炸数据包崩溃设置
  ExplosionsCrash:
    # Whether to kick out the player after canceling all packets sent using the explorations command (kick out packets are only visible to the server). Default: false
    # 使用explosions命令后取消所有数据包发送后是否踢出玩家（踢出数据包只有服务端可见）。默认: false
    CancelKick: false
    # Send Packet Limit. Default: 99
    # 发送的数据包数量。默认：99
    PacketLimit: 99
  # Frozen settings
  # Frozen设置
  Frozen:
    # The size of the Frozen inventory, a multiple of 9, must not exceed 126. Default: 126
    # Frozen界面的大小，9的倍数，不得超过126。默认：126
    FrozenGUISize: 126
    # Is the item in the Frozen inventory enchanted with light effects. Default: true
    # Frozen界面的物品是否附魔光效。默认：true
    FrozenGUIItemEnchant: true
    # The item name on the Frozen inventory (in uppercase English) Default: BEDROCK
    # Frozen界面的物品名（英文大写）默认：WOOL
    FrozenGUIItem: "WOOL"
    # The quantity of each item in the Frozen inventory. Default: -1
    # Frozen界面的每个物品数量。默认：-1
    FrozenGUIItemAmount: -1
    # Frozen inventory item [durability?], format String. Default: 0
    # Frozen界面物品[?耐久度?], 格式String。默认：0
    FrozenGUIItemDamage: "0"
    #F rozen inventory item ID, for example, red wool is 14. Default: 14
    # Frozen界面物品ID，例如红色羊毛为14.默认：14
    FrozenGUIItemByte: "14"
    # Frozen time exits the switch for executing commands on the server. Default: true
    # Frozen时间退出服务器执行命令的开关。默认：true
    QuitCommandBoolean: true
    # The command to exit the server at Frozen time. (Replace player name with% player%) Default: ban% player%
    # Frozen时间退出服务器执行的命令。（使用%player%替换玩家名称）默认：ban %player%
    QuitCommandList:
      - "ban %player%"
# Text(Support color char '&').
# 文本（支持彩色字符'&'）。
Messages:
  #Kick out the player's text. Default: Time Out
  # 踢出玩家的文本。默认：Time Out
  TImeOutKick: "Time Out"
  # The text indicating the successful execution of the command. Default:&aDone
  # 命令运行成功的文本。默认：&aDone
  CommandDone: "&aDone"
  # Not enough permission text. Default:&cYou dont have permission to execute this command
  # 没有足够的权限文本。默认：&cYou dont have permission to execccute this command.
  CommandNoPermission: "&cYou dont have permission to execccute this command."
  # Remove the text of players from the crash list. Default: &4Player has been removed form crashlist! if you want re-add to crashlist please re-type this command.
  # 移除crashlist列表玩家的文本。默认：&4Player has been removed form crashlist! if you want re-add to crashlist please re-type this command.
  CommandRemoveCrashList: "&cPlayer has been removed form crashlist! if you want re-add to crashlist please re-type this command."
  # The player is not online. Default:&cPlayer is not online.
  # 玩家不在线。默认：&cPlayer is not online.
  CommandPlayerNotOnline: "&cPlayer is not online."
  #The title of the Frozen inventory. [Warning] Do not set it too simply as it may be replaced by other plugins or bypassed by players. Default: &4&lYou have been Frozen.
  # Frozen背包的标题。[警告] 不要设置太简单，可能会被其他插件顶替或被玩家绕过。默认：&4&lYou have been Frozen.
  FrozenGUITitle: "&4&lYou have been Frozen."
  # The item display name of Frozen inventory. Default:&4&lYou have been Frozen.
  # Frozen背包的物品展示名称。默认：&4&lYou have been Frozen.
  FrozenGUIItemName: "&4&lYou have been Frozen."
  # The display properties of Frozen inventory [Reminder] List. Default:&cDENT CLOSE YOU GAME
  # Frozen背包的展示属性. [提醒] 列表。默认：&cDONT CLOSE YOUR GAME
  FrozenGUIItemLores:
    - "&cDONT CLOSE YOUR GAME"
  # Remove player Frozen text. Default: &cPlayer has been removed form frozen! if you want re-frozen please re-type this command.
  # 移除玩家Frozen的文本。默认：&cPlayer has been removed form frozen! if you want re-frozen please re-type this command.
  CommandFrozenRemove: "&cPlayer has been removed form frozen! if you want re-frozen please re-type this command."
  # The plugin generates error messages and sends them to the player as text. Default:&4&lA ERROR HAS SPAWNED IN CONSOLE!
  # 插件生成报错发给玩家的文本。默认：&4&lA ERROR HAS SPAWNED IN CONSOLE!
  ERROR: "&4&lA ERROR HAS SPAWNED IN CONSOLE!"

# 开发者设置
# Develop Settings
Debug:
  # Config Version(Dont Edit)
  # 配置文件版本（禁止修改）
  CfgVer: 1.0
  # bStats.org Default: true
  # bStats.org 默认：true
  bStats: true
  # Packet listening
  # 数据包监听
  PacketListener:
    # Listen for printing mode when canceling data packets. Default: false
    # 数据包取消时监听打印模式。默认：false
    mode: false
# More is coming soon
# 更多的正在制作
```


启动日志：
```
[22:41:49 INFO]: [Crasher] Loading Crasher v1.0
[22:41:49 INFO]: [Crasher] Your server version: v1_8_R3
[22:41:49 WARN]: [Crasher] Default system encoding may have misread config.yml from plugin jar
[22:41:49 INFO]: [Crasher] Loaded Crasher v1.0
[22:41:49 INFO]: [Crasher] Enabling Crasher v1.0
[22:41:49 INFO]: [Crasher] Enabled Crasher v1.0
```


_本插件所用所有代码均为原创,不存在借用/抄袭等行为
请勿用来崩正常玩家客户端！_
