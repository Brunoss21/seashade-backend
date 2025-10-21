package com.seashade.api_seashade.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service 
public class EmailService {

    private final JavaMailSender mailSender; 

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 
     * @param toEmail 
     * @param nomeAtendente 
     * @param codigoAcesso 
     */
    public void sendAccessCodeEmail(String toEmail, String nomeAtendente, String codigoAcesso) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            message.setFrom("seashadebr@gmail.com");
            
            message.setTo(toEmail);
            
            message.setSubject("Seu Código de Acesso - Sistema Seashade");
            
            String emailBody = String.format(
                "Olá, %s!\n\n" +
                "Seu cadastro como atendente no sistema Seashade foi concluído.\n\n" +
                "Seu código de acesso único é: %s\n\n" +
                "Use este código para fazer login na tela de atendentes do sistema.\n\n" +
                "Atenciosamente,\n" +
                "Equipe Seashade",
                nomeAtendente,
                codigoAcesso
            );
            
            message.setText(emailBody);
        
            mailSender.send(message);
            
            System.out.println("E-mail com código de acesso enviado com sucesso para: " + toEmail);
            
        } catch (Exception e) {
            // Tratamento de erro básico (criar logs mais robustos no futuro)
            System.err.println("Erro ao enviar e-mail para " + toEmail + ": " + e.getMessage());
        }
    }
}
