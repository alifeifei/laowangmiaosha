package com.laowang.kill.server.service.impl;

import com.laowang.kill.server.dto.MailDto;
import com.laowang.kill.server.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@Service

public class MailServiceImpl implements MailService {


    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private Environment env;

    @Override

    public void sendSimpleEmail(MailDto dto) {
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(env.getProperty("mail.send.from"));
            message.setTo(dto.getTos());
            message.setSubject(dto.getSubject());
            message.setText(dto.getContent());
            mailSender.send(message);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override

    public void sendHTMLMail(MailDto dto) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper messageHelper=new MimeMessageHelper(message,true,"utf-8");
            messageHelper.setFrom(env.getProperty("mail.send.from"));
            System.out.println(dto.getTos()[0]);
            messageHelper.setTo(dto.getTos());
            messageHelper.setSubject(dto.getSubject());
            messageHelper.setText(dto.getContent(),true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
