

## 为什么写这系列文章？
网上 socket 的demo都是浅尝即止，随便抛出一个 socket 的基本连接就完事了。也不考虑线程、并发问题，也不说明配置问题；
每次查看都每次痛苦，所以，这里通过学习，也记录一下笔记。

目前的时间计划是这样： 

 1. Socket 系列文章和实例
 2. NIO 系列文章和实例
 3. Netty 系列文章和实例

### Socket 系列：
[Android Socket通信(一)  -- 初识与相遇](https://blog.csdn.net/u011418943/article/details/92612997) 
该例子比较简单，写了 socket 客户端与服务器之间的简单操作以及相关介绍。
[Android Socket通信(二) --UDP，单播，广播和多播(组播)](https://blog.csdn.net/u011418943/article/details/92839617)
先讲解了 udp 的基础操作，然后学习 udp 的广播和组播，你会发现局域网的东西，ip 和 端口都是可以通过 udp 广播去拿到的。
[Android Socket通信(三) -- TCP 配置和传递基础数据](https://blog.csdn.net/u011418943/article/details/93157556)
第三章，先学习了  tcp 的一些基础配置和解释，然后用 ByteBuffer 去传递基础数据，嗯，在一些高效的网络数据传输中，byte 传输大于天。
[Android Socket通信(四) -- UDP与TCP结合传输数据](https://blog.csdn.net/u011418943/article/details/93523767)
第四章，则是一个案例了，假如一个场景，你需要通过 udp 广播对象的 IP 和 端口，再用 tcp 进行数据传输，笔者就是用它做了一个 Android 与 Android 局域网内的简单协同白板，效果还不错。
[Android Socket通信(五) -- 实现一个多人聊天室](https://blog.csdn.net/u011418943/article/details/93881970)
相当于一个总结，一个简单的聊天室。

### NIO 系列文章
[Android NIO 系列教程(一)  NIO概述](https://blog.csdn.net/u011418943/article/details/94381120)

[Android NIO 系列教程(二)  -- Channel](https://blog.csdn.net/u011418943/article/details/94385590)

[Android NIO 系列教程(三) -- Buffer](https://blog.csdn.net/u011418943/article/details/94393512)

[Android NIO 系列教程(四) -- Selector](https://blog.csdn.net/u011418943/article/details/94396302)

[Android NIO 系列教程(五) -- FileChannel](https://blog.csdn.net/u011418943/article/details/94436664)

[Android NIO 系列教程(六) -- SocketChannel](https://blog.csdn.net/u011418943/article/details/94442382)

[Android NIO 系列教程(七) -- ServerSocketChannel](https://blog.csdn.net/u011418943/article/details/94443778)

[Android NIO 系列教程(七) -- ServerSocketChannel](https://blog.csdn.net/u011418943/article/details/94443778)
### Netty 系列文章
未完待续。。

