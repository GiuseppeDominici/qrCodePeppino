package com.library.qrcodepeppino.Controller;

import com.google.zxing.WriterException;
import com.library.qrcodepeppino.Model.RequestData;
import com.library.qrcodepeppino.Model.ResponseImage;
import com.library.qrcodepeppino.Utils.MethodUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/qr_code")
public class QrCodeController {

    @PostMapping("/generate")
    public ResponseEntity<Object> downloadQrCodeBase64(@RequestBody RequestData requestData) {
        try {
            byte[] qrCodeBytes = MethodUtils.qrCodeResult(requestData);
            ResponseImage response = MethodUtils.result(qrCodeBytes);
            return ResponseEntity.ok(response);
        } catch (RuntimeException | WriterException | IOException e) {
            assert e instanceof RuntimeException;
            return MethodUtils.handleRuntimeException((RuntimeException) e);
        }
    }


}
 
