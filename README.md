<p>本文参照自：<a href="http://blog.csdn.net/sasoritattoo/article/details/8285926" title="Android 连接Wifi和创建Wifi热点 demo ">Android 连接Wifi和创建Wifi热点 demo</a>.</p>

+ android的热点功能不可见，用了反射的技术搞定之外。  
+ Eclipse设置语言为utf-8才能查看中文注释 

在Android手机上可以通过在收方开启一个wifi热点,然后再发送方连接这个wifi热点。
这样他们就在一个局域网，然后通过socket进行通信。

本文的demo程序写得比较简单。

对于收方，首先点击“创建wifi热点”按钮，开启一个wifi热点，然后点击“开启接受”按钮，准备接受数据。
图1为收方

![alt text](http://img0.ph.126.net/GSNIwWnUJ97ARGhuDqZpBg==/1409345208490597254.png "图1")


 
对于发方，首先点击“连接wifi热点”按钮，连接收方的wifi热点，然后点击“开启发送”按钮，开始发送数据。
图2为发方。
![alt text](http://img1.ph.126.net/uWZa6gz4nQ1LrbsEs4xC-w==/4843902874313781565.png "图2")
 
