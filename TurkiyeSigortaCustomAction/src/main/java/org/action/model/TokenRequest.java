package org.action.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenRequest {

    private String tk1;
    private String tk2;
    private String tk3;
    private String apiUrl;
    private String log_kimlik_no;
    private String log_musteri_no;
}
