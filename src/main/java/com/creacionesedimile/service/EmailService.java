package com.creacionesedimile.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio para el envío de correos electrónicos transaccionales.
 *
 * Configuración requerida en application.properties:
 *   spring.mail.host, spring.mail.port, spring.mail.username, spring.mail.password
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    /**
     * Dirección remitente verificada en Brevo (Senders & IPs).
     * Configúrala en application.properties con: app.mail.from=tu@email.com
     * Si no se define, usa el login SMTP (puede ser rechazado por Brevo).
     */
    @Value("${app.mail.from:${spring.mail.username}}")
    private String fromAddress;

    @Value("${app.nombre:Creaciones Edimile}")
    private String appNombre;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía el correo de recuperación de contraseña con el enlace de reset.
     *
     * @param destinatario Email del usuario
     * @param resetUrl     URL completa del enlace de restablecimiento
     */
    public void enviarRecuperacionContrasena(String destinatario, String resetUrl) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(fromAddress, appNombre);
            helper.setTo(destinatario);
            helper.setSubject("Restablecer contraseña — " + appNombre);
            helper.setText(construirCuerpoHtml(resetUrl), true);

            mailSender.send(mensaje);
            log.info("Correo de recuperación enviado a {}", destinatario);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando correo de recuperación a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de recuperación.", e);
        }
    }

    // ── Plantilla HTML del correo ────────────────────────────────────────────

    private String construirCuerpoHtml(String resetUrl) {
        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f4f4;padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,.08);">

                          <!-- Cabecera -->
                          <tr>
                            <td style="background:#000000;padding:28px 32px;text-align:center;">
                              <h1 style="margin:0;color:#ffffff;font-size:20px;letter-spacing:1px;">
                                CREACIONES EDIMILE
                              </h1>
                              <p style="margin:6px 0 0;color:rgba(255,255,255,.55);font-size:12px;">
                                Sistema de Gestión
                              </p>
                            </td>
                          </tr>

                          <!-- Cuerpo -->
                          <tr>
                            <td style="padding:36px 32px;">
                              <p style="margin:0 0 12px;font-size:16px;color:#111;font-weight:600;">
                                Solicitud de restablecimiento de contraseña
                              </p>
                              <p style="margin:0 0 24px;font-size:14px;color:#555;line-height:1.6;">
                                Recibimos una solicitud para restablecer la contraseña de tu cuenta.
                                Haz clic en el botón a continuación para establecer una nueva contraseña.
                                Este enlace tiene vigencia de <strong>1 hora</strong>.
                              </p>

                              <!-- Botón -->
                              <table cellpadding="0" cellspacing="0" style="margin:0 auto 28px;">
                                <tr>
                                  <td style="background:#198754;border-radius:8px;">
                                    <a href="%s"
                                       style="display:inline-block;padding:14px 32px;color:#ffffff;
                                              font-size:14px;font-weight:600;text-decoration:none;
                                              letter-spacing:.3px;">
                                      Restablecer contraseña
                                    </a>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:0 0 8px;font-size:12px;color:#888;line-height:1.5;">
                                Si el botón no funciona, copia y pega este enlace en tu navegador:
                              </p>
                              <p style="margin:0 0 24px;font-size:11px;color:#aaa;word-break:break-all;">
                                %s
                              </p>

                              <p style="margin:0;font-size:12px;color:#aaa;line-height:1.5;">
                                Si no solicitaste este cambio, ignora este mensaje. Tu contraseña permanecerá sin cambios.
                              </p>
                            </td>
                          </tr>

                          <!-- Pie -->
                          <tr>
                            <td style="background:#f8f8f8;padding:16px 32px;text-align:center;
                                       border-top:1px solid #ebebeb;">
                              <p style="margin:0;font-size:11px;color:#bbb;">
                                © 2025 Creaciones Edimile · Este es un correo automático, no responder.
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(resetUrl, resetUrl);
    }
}
