package com.laowang.kill.server.service;

import com.laowang.kill.server.dto.MailDto;

public interface MailService {
    void sendSimpleEmail(MailDto dto);

    void sendHTMLMail(MailDto dto);
}
