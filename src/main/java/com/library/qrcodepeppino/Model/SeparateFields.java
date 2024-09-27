package com.library.qrcodepeppino.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeparateFields {
    
    private ArrayList<String> stringFields;
    private ArrayList<Integer> intFields;

}
