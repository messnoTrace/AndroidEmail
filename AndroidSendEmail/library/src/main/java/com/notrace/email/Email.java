package com.notrace.email;

import android.text.TextUtils;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Created by notrace on 2017/3/28.
 */

public class Email {
    private String toMail;//收件人
    private String fromMail;//发件人
    private String server;//发件人的邮箱的 SMTP 服务器地址
    private String userName;//用户名
    private String password;//密码
    private String title;//标题
    private String body;//内容
    private String attachment;//附件

    Email(Builder builder){
        this.toMail=builder.toMail;
        this.fromMail=builder.fromMail;
        this.server=builder.server;
        this.userName=builder.userName;
        this.body=builder.body;
        this.password=builder.password;
        this.attachment=builder.attachment;
        this.title=builder.title;
    }


    public static class Builder{
        private String toMail;
        private String fromMail;
        private String server;
        private String userName;
        private String password;
        private String title;
        private String body;
        private String attachment;

        public Builder(){

        }


       public Builder toMail(String toMail){
           this.toMail=toMail;
           return this;
       }
       public Builder fromMail(String fromMail){
           this.fromMail=fromMail;
           return this;
       }
       public Builder server(String server){
           this.server=server;
           return  this;
       }
       public Builder userName(String userName){
           this.userName=userName;
           return this;
       }
       public Builder password(String password){
           this.password=password;
           return this;
       }
       public Builder title(String title){
           this.title=title;
           return this;
       }
       public Builder body(String body){
           this.body=body;
           return this;
       }
       public Builder attachment(String attachment){
           this.attachment=attachment;
           return this;
       }
        public Email build()
        {
            return new Email(this);
        }

    }

    public void send()throws Exception{

        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props =System.getProperties();                   // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", server);   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证

        // PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
        //     如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
        //     打开下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。
        /*
        // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
        //                  需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助,
        //                  QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)
        final String smtpPort = "465";
        props.setProperty("mail.smtp.port", smtpPort);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.port", smtpPort);
        */

        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getDefaultInstance(props);
        session.setDebug(true);                                 // 设置为debug模式, 可以查看详细的发送 log

        // 3. 创建一封邮件
        MimeMessage message;
        if(TextUtils.isEmpty(attachment))
        {
            message = createSimpleMessage(session, fromMail, toMail);
        }else
        {
            message = createMessageWithAttach(session, fromMail, toMail);
        }


        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();

        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
        //
        //    PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
        //           仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
        //           类型到对应邮件服务器的帮助网站上查看具体失败原因。
        //
        //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
        //           (1) 邮箱没有开启 SMTP 服务;
        //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
        //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
        //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
        //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
        //
        //    PS_03: 仔细看log, 认真看log, 看懂log, 错误原因都在log已说明。
        transport.connect(userName, password);

        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人


        //TODO 加上这句是因为
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);

        transport.sendMessage(message, message.getAllRecipients());

        // 7. 关闭连接
        transport.close();
    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session 和服务器交互的会话
     * @param sendMail 发件人邮箱
     * @param receiveMail 收件人邮箱
     * @return
     * @throws Exception
     */
    private MimeMessage createSimpleMessage(Session session, String sendMail, String receiveMail) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "UTF-8"));

        // 4. Subject: 邮件主题
        message.setSubject(title,"UTF-8");

        // 5. text: 邮件正文
        message.setText(body,"utf-8");

        // 6. 设置发件时间
        message.setSentDate(new Date());

        // 7. 保存设置
        message.saveChanges();

        return message;
    }

    /**
     * 创建一封包含附件的邮件
     *
     * @param session 和服务器交互的会话
     * @param sendMail 发件人邮箱
     * @param receiveMail 收件人邮箱
     * @return
     * @throws Exception
     */
    public  MimeMessage createMessageWithAttach(Session session, String sendMail, String receiveMail) throws Exception{

        // 1. 创建邮件对象
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "UTF-8"));

        // 4. Subject: 邮件主题
        message.setSubject(title, "UTF-8");


        // 5. 创建附件“节点”


        MimeBodyPart attachPart = new MimeBodyPart();
        //添加附件
        FileDataSource fds = new FileDataSource(attachment);
        attachPart.setDataHandler(new DataHandler(fds));//附件
        attachPart.setFileName(MimeUtility.encodeWord(fds.getName()));

        MimeBodyPart textBodyPart = new MimeBodyPart();
        //添加邮件内容
        textBodyPart.setText(body);

        MimeMultipart allMultipart = new MimeMultipart();
        allMultipart.addBodyPart(attachPart);
        allMultipart.addBodyPart(textBodyPart);
        allMultipart.setSubType("mixed");
        message.setContent(allMultipart);
        message.setSentDate(new Date());
        message.saveChanges();




        return message;
    }

    /**
     * 创建一封复杂邮件（文本+图片+附件）
     * @param session
     * @param sendMail
     * @param receiveMail
     * @return
     * @throws Exception
     */
//    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail) throws Exception {
//        // 1. 创建邮件对象
//        MimeMessage message = new MimeMessage(session);
//
//        // 2. From: 发件人
//        message.setFrom(new InternetAddress(sendMail, "我的测试邮件_发件人昵称", "UTF-8"));
//
//        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
//        message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "我的测试邮件_收件人昵称", "UTF-8"));
//
//        // 4. Subject: 邮件主题
//        message.setSubject("TEST邮件主题（文本+图片+附件）", "UTF-8");
//
//        /*
//         * 下面是邮件内容的创建:
//         */
//
//        // 5. 创建图片“节点”
//        MimeBodyPart image = new MimeBodyPart();
//        DataHandler dh = new DataHandler(new FileDataSource("FairyTail.jpg")); // 读取本地文件
//        image.setDataHandler(dh);                   // 将图片数据添加到“节点”
//        image.setContentID("image_fairy_tail");     // 为“节点”设置一个唯一编号（在文本“节点”将引用该ID）
//
//        // 6. 创建文本“节点”
//        MimeBodyPart text = new MimeBodyPart();
//        //    这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
//        text.setContent("这是一张图片<br/><img src='cid:image_fairy_tail'/>", "text/html;charset=UTF-8");
//
//        // 7. （文本+图片）设置 文本 和 图片 “节点”的关系（将 文本 和 图片 “节点”合成一个混合“节点”）
//        MimeMultipart mm_text_image = new MimeMultipart();
//        mm_text_image.addBodyPart(text);
//        mm_text_image.addBodyPart(image);
//        mm_text_image.setSubType("related");    // 关联关系
//
//        // 8. 将 文本+图片 的混合“节点”封装成一个普通“节点”
//        //    最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
//        //    上面的 mm_text_image 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
//        MimeBodyPart text_image = new MimeBodyPart();
//        text_image.setContent(mm_text_image);
//
//        // 9. 创建附件“节点”
//        MimeBodyPart attachment = new MimeBodyPart();
//        DataHandler dh2 = new DataHandler(new FileDataSource("妖精的尾巴目录.doc"));  // 读取本地文件
//        attachment.setDataHandler(dh2);                                             // 将附件数据添加到“节点”
//        attachment.setFileName(MimeUtility.encodeText(dh2.getName()));              // 设置附件的文件名（需要编码）
//
//        // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合“节点” / Multipart ）
//        MimeMultipart mm = new MimeMultipart();
//        mm.addBodyPart(text_image);
//        mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
//        mm.setSubType("mixed");         // 混合关系
//
//        // 11. 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
//        message.setContent(mm);
//
//        // 12. 设置发件时间
//        message.setSentDate(new Date());
//
//        // 13. 保存上面的所有设置
//        message.saveChanges();
//
//        return message;
//    }



}
