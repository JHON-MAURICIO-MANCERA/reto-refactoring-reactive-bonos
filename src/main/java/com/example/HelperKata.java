package com.example;


import reactor.core.publisher.Flux;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.stream.Stream;


public class HelperKata {
    private static final  String EMPTY_STRING = "";
    private static String ANTERIOR_BONO = null;
    private static Set<String> codes = new HashSet<>();
    private static  AtomicInteger counter = new AtomicInteger(0);

    public static Flux<CouponDetailDto> getListFromBase64File(final String fileBase64) {
        AtomicInteger counter = new AtomicInteger(0);
        String characterSeparated = FileCSVEnum.CHARACTER_DEFAULT.getId();

          return createFluxFrom(fileBase64).skip(1)
                            .map(HelperKata::createBonoEntity)
                            .map(modelBonoEntity -> {

                                String errorMessage =validateError(codes,modelBonoEntity);
                                String dateValidated = conditionOfBoolean(errorMessage == null, modelBonoEntity.getDate(), null);

                                return   CouponDetailDto.aCouponDetailDto()
                                        .withCode(modelBonoEntity.getCode() )
                                        .withDueDate(dateValidated)
                                        .withNumberLine(counter.incrementAndGet())
                                        .withMessageError(errorMessage)
                                        .withTotalLinesFile(1)
                                        .build();
                            });


                                     /*


                                        if (ANTERIOR_BONO == null || ANTERIOR_BONO.equals("")) {
                                            ANTERIOR_BONO = typeBono(bonoEnviado);
                                            if (ANTERIOR_BONO == "") {
                                                bonoForObject = null;
                                            } else {
                                                bonoForObject = bonoEnviado;
                                            }
                                        } else if (ANTERIOR_BONO.equals(typeBono(bonoEnviado))) {
                                            bonoForObject = bonoEnviado;
                                        } else if (!ANTERIOR_BONO.equals(typeBono(bonoEnviado))) {
                                            bonoForObject = null;
                                        }
*/

    }

    private static String conditionOfBoolean(boolean b, String date, String o) {
        return (b) ? date : o;
    }

    private static Flux<String> createFluxFrom(String fileBase64) {
        return Flux.using(
                () -> new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream(decodeBase64(fileBase64))
                )).lines(),
                Flux::fromStream,
                Stream::close
        );
    }
    private static ModelBonoEntity createBonoEntity (String line) {
        String characterSeparated = FileCSVEnum.CHARACTER_DEFAULT.getId();
        var row=Optional.of(List.of(line.split(characterSeparated))); // tiene creada la fila con la coma
        return new ModelBonoEntity(validcodeEmpty(row),validDateEmpty(row));// crea un modelo (bono,date)
        //return completeCoupon(array) ? new ModelBonoEntity(array.get(0), array.get(1)) : incompleteCoupon(array);
    }
//// valida codigo no vacio o vacio
    private  static String validcodeEmpty(Optional<List<String>> row){
      return  row.filter(colums -> !colums.isEmpty())
               .map(colums-> colums.get(0))
               .orElse(EMPTY_STRING);

    }
    //valida la fecha vacia o no vacia
    private  static String validDateEmpty(Optional<List<String>> row){
        return   row.filter(colums -> !colums.isEmpty() )
                .map(colums-> colums.get(1))
                .orElse(EMPTY_STRING);

    }
    private static boolean validateBonoCodDatIsBlank(ModelBonoEntity modelBonoEntity) {
        return modelBonoEntity.getCode().isBlank() || modelBonoEntity.getDate().isBlank();
    }
    private static String validateError(Set<String> codes, ModelBonoEntity modelBonoEntity) {
        if (validateBonoCodDatIsBlank(modelBonoEntity)) {
            return ExperienceErrorsEnum.FILE_ERROR_COLUMN_EMPTY.toString();
        }
        return conditionOfBoolean(codes.add(modelBonoEntity.getCode()), dateValidate(modelBonoEntity), ExperienceErrorsEnum.FILE_ERROR_CODE_DUPLICATE.toString());

    }
private static String dateValidate(ModelBonoEntity modelBonoEntity){
    if (!validateDateRegex(modelBonoEntity.getDate())){
        return   ExperienceErrorsEnum.FILE_ERROR_DATE_PARSE.toString();
    }
     return conditionOfBoolean(validateDateIsMinor(modelBonoEntity.getDate()), ExperienceErrorsEnum.FILE_DATE_IS_MINOR_OR_EQUALS.toString(), null);
}



    public static String typeBono(String bonoIn) {
        if (bonoIn.chars().allMatch(Character::isDigit)
                && bonoIn.length() >= 12
                && bonoIn.length() <= 13) {
            return ValidateCouponEnum.EAN_13.getTypeOfEnum();
        }
        if (bonoIn.startsWith("*")
                && bonoIn.replace("*", "").length() >= 1
                && bonoIn.replace("*", "").length() <= 43) {
            return ValidateCouponEnum.EAN_39.getTypeOfEnum();

        }
        else {
            return ValidateCouponEnum.ALPHANUMERIC.getTypeOfEnum();
        }
    }

    public static boolean validateDateRegex(String dateForValidate) {
        String regex = FileCSVEnum.PATTERN_DATE_DEFAULT.getId();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dateForValidate);
        return matcher.matches();
    }

    private static byte[] decodeBase64(final String fileBase64) {
        return Base64.getDecoder().decode(fileBase64);

    }



    public static boolean validateDateIsMinor(String dateForValidate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(FileCSVEnum.PATTERN_SIMPLE_DATE_FORMAT.getId());
            Date dateActual = sdf.parse(sdf.format(new Date()));
            Date dateCompare = sdf.parse(dateForValidate);
            if (dateCompare.compareTo(dateActual) < 0) {
                return true;
            } else if (dateCompare.compareTo(dateActual) == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

}
