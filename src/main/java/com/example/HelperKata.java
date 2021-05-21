package com.example;


import reactor.core.publisher.Flux;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.stream.Stream;

import static java.util.Objects.isNull;


public class HelperKata {
      private static String ANTERIOR_BONO = null;
    final static   Set<String> codes = new HashSet<>();
    final static AtomicInteger counter = new AtomicInteger(0);


    public static Flux<CouponDetailDto> getListFromBase64File(final String fileBase64) {


        return createFluxFrom(fileBase64).skip(1)
                .map(HelperKata::createBonoEntity)
                .map(modelBono -> CouponDetailDto.aCouponDetailDto()
                        .withCode(createBonoForObject(modelBono))
                        .withMessageError(validateError(codes,modelBono))
                        .withDueDate(conditionOfBoolean(validateError(codes,modelBono) ==(null),modelBono.getDate(),null))
                        .withTotalLinesFile(1)
                        .withNumberLine(counter.incrementAndGet())
                        .build());


                }
// TODO: metodo general para validar un boolean y retornar OTRA FUNCION O UNA ASIGNACION
    private static String conditionOfBoolean(boolean condicion, String outByTrue, String outByFalse) {
        return (condicion) ? outByTrue : outByFalse;
    }
// TODO: CREACION DEL FLUX A PARTIR DEL ARCHIVO BASE 64
    private static Flux<String> createFluxFrom(String fileBase64) {
        return Flux.using(
                () -> new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream(decodeBase64(fileBase64))
                )).lines(),
                Flux::fromStream,
                Stream::close
        );
    }

    private static ModelBonoEntity createBonoEntity(String line) {
        String characterSeparated = FileCSVEnum.CHARACTER_DEFAULT.getId();
        var row = Optional.of(List.of(line.split(characterSeparated))); // tiene creada la fila con la coma
        return new ModelBonoEntity(validcodeEmpty(row), validDateEmpty(row));// crea un modelo (bono,date)

    }

    // valida codigo no vacio o vacio
    private static String validcodeEmpty(Optional<List<String>> row) {
        return row.filter(code -> !code.isEmpty())
                .map(code -> code.get(0))
                .orElse("");
    }

    //valida la fecha vacia o no vacia
    private static String validDateEmpty(Optional<List<String>> row) {
        return row.filter(colums -> !colums.isEmpty())
                .map(colums -> colums.get(1))
                .orElse("");
    }

    //todo:valida si el bono completo está vacio

    private static boolean validateBonoCodAndDatIsBlank(ModelBonoEntity modelBonoEntity) {

        return modelBonoEntity.getCode().isBlank() || modelBonoEntity.getDate().isBlank();
    }
    // TODO: ESTE BLOQUE ES DE CREACION DEL ERROR DE ACUERDO AL CODIGO Y A LA FECHA
    private static String validateError(Set<String> codes, ModelBonoEntity modelBonoEntity ) {

        if (validateBonoCodAndDatIsBlank(modelBonoEntity)) {
            return  ExperienceErrorsEnum.FILE_ERROR_COLUMN_EMPTY.toString();
        }
        return conditionOfBoolean(codes.add(modelBonoEntity.getCode()), dateValidate(modelBonoEntity), ExperienceErrorsEnum.FILE_ERROR_CODE_DUPLICATE.toString());

    }
    private static String dateValidate(ModelBonoEntity modelBonoEntity) {
        if (!validateDateRegex(modelBonoEntity.getDate())) {
            return ExperienceErrorsEnum.FILE_ERROR_DATE_PARSE.toString();
        }
        return conditionOfBoolean(validateDateIsMinor(modelBonoEntity.getDate()), ExperienceErrorsEnum.FILE_DATE_IS_MINOR_OR_EQUALS.toString(), null);
    }

//TODO:VALIDA QUE TIPO DE CODIGO LLEGA
    public static String typeBono(String bonoIn) {
        if (bonoIn.matches(ValidateCouponEnum.ALPHANUMERIC.getTypeOfEnum())) {
            return ValidateCouponEnum.ALPHANUMERIC.getTypeOfEnum();}

        return  (boInEAN_39(bonoIn))?
                ValidateCouponEnum.EAN_39.getTypeOfEnum()
               : ValidateCouponEnum.EAN_13.getTypeOfEnum();
    }

    private static boolean boInEAN_39(String codeIn) {
        return  (codeIn.startsWith("*")) && (validateLengthCode(codeIn) );
    }
    private static boolean validateLengthCode (String codeIn){
        return codeIn.replace("*", "").length() >= 1
            && codeIn.replace("*", "").length() <= 43;}


    //TODO: VALIDA EL FORMATO DE LA FECHA
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
            return  (dateCompare.compareTo(dateActual) < 0);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
//TODO: DEFINE EL CODIGO ANTERIOR Y ASIGNA A BONOFOROBJECT
    private static String createBonoForObject(ModelBonoEntity modelBonoEntity) {
        String codeActuality = modelBonoEntity.getCode();
        String bonoForObject;
        if (isNull(ANTERIOR_BONO)) {
            ANTERIOR_BONO = typeBono(codeActuality);
            bonoForObject = codeActuality;
        } else {
            bonoForObject = ANTERIOR_BONO.equals(typeBono(codeActuality)) ? codeActuality : null;
        }
        return bonoForObject;
    }

}
