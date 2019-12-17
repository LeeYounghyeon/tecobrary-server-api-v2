package com.woowacourse.tecobrary.serial.ui;

import com.woowacourse.tecobrary.common.util.AcceptanceTestUtils;
import com.woowacourse.tecobrary.serial.ui.dto.SerialCreateRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import static com.woowacourse.tecobrary.serial.exception.NotFoundSerialTargetException.NOT_FOUND_SERIAL_TARGET_EXCEPTION_MESSAGE;
import static com.woowacourse.tecobrary.serial.exception.UniqueConstraintException.UNIQUE_CONSTRAINT_EXCEPTION_MESSAGE;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

class SerialCreateReadControllerTest extends AcceptanceTestUtils {

    @DisplayName("[GET] /serials?bookId, 성공적으로 해당 bookId 에 대한 책의 목록을 응답 받는다.")
    @Test
    void successfullyGetBookIdSerials() {
        given(this.spec).
                queryParam("bookId", 9).
                filter(document(DOCUMENTATION_OUTPUT_DIRECTORY,
                        requestParameters(
                                parameterWithName("bookId").description("book_id")),
                        responseFields(
                                fieldWithPath("[0].serialNumber").description("book_serial_number"),
                                fieldWithPath("[0].status").description("book_rent_status")))).
        when().
                get(baseUrl("/serials")).
        then().
                log().everything().
                log().ifError().
                log().ifValidationFails().
                contentType(JSON).
                body("size()", equalTo(4)).
                body("[0].serialNumber", equalTo(10)).
                body("[0].status", is(false)).
                body("[2].serialNumber", equalTo(12)).
                body("[2].status", is(false));
    }

    @DisplayName("[GET] /serials?bookId=10000000, 존재하지 않는 책에 대하여 Bad Request 응답을 받는다.")
    @Test
    void failedGetBookIdSerialsBadRequest() {
        given().
                queryParam("bookId", 10_000_000).
        when().
                get(baseUrl("/serials")).
        then().
                log().ifError().
                log().ifValidationFails().
                contentType(JSON).
                body("message", is(NOT_FOUND_SERIAL_TARGET_EXCEPTION_MESSAGE));
    }

    @DisplayName("[POST] /serials, id 에 해당하는 도서에 serial 을 추가한다.")
    @DirtiesContext
    @Test
    void successfullyCreateSerial() {
        SerialCreateRequestDto serialCreateRequestDto = new SerialCreateRequestDto(1L, 1000L);
        given(this.spec).
                contentType(JSON).
                body(serialCreateRequestDto).
                filter(document(DOCUMENTATION_OUTPUT_DIRECTORY,
                        requestFields(
                                fieldWithPath("bookId").description("target_book_id"),
                                fieldWithPath("serialNumber").description("new_serial_number")),
                        responseFields(
                                fieldWithPath("message").description("등록에 성공하였습니다."),
                                fieldWithPath("serial.status").description("book_rent_status"),
                                fieldWithPath("serial.id").description("serial_id"),
                                fieldWithPath("serial.serialNumber").description("serial_number"),
                                fieldWithPath("serial.bookId").description("book_id"),
                                fieldWithPath("serial.updatedAt").description("serial_update_time"),
                                fieldWithPath("serial.createdAt").description("serial_create_time")))).
        when().
                post(baseUrl("/serials")).
        then().
                log().ifError().
                log().ifValidationFails().
                statusCode(200).
                contentType(JSON).
                body("message", is("등록에 성공하였습니다.")).
                body("serial.status", is(false)).
                body("serial.serialNumber", is(1000)).
                body("serial.bookId", is(1)).
                body("serial.updatedAt", notNullValue()).
                body("serial.createdAt", notNullValue());
    }

    @DisplayName("[POST] /serials, id에 해당하는 도서가 없을 경우 serial 등록에 실패한다.")
    @Test
    void failedCreateSerial_NotFoundSerialTarget() {
        SerialCreateRequestDto serialCreateRequestDto = new SerialCreateRequestDto(1_000_000L, 1000L);
        given().
                contentType(JSON).
                body(serialCreateRequestDto).
        when().
                post(baseUrl("/serials")).
        then().
                log().ifError().
                log().ifValidationFails().
                statusCode(400).
                contentType(JSON).
                body("message", is(NOT_FOUND_SERIAL_TARGET_EXCEPTION_MESSAGE));
    }

    @DisplayName("[POST] /serials, 일련번호가 존재하는 경우 serial 등록에 실패한다.")
    @Test
    void failedCreateSerial_UniqueConstraint() {
        SerialCreateRequestDto serialCreateRequestDto = new SerialCreateRequestDto(1L, 1L);
        given().
                contentType(JSON).
                body(serialCreateRequestDto).
        when().
                post(baseUrl("/serials")).
        then().
                log().ifError().
                log().ifValidationFails().
                statusCode(400).
                contentType(JSON).
                body("message", is(UNIQUE_CONSTRAINT_EXCEPTION_MESSAGE));
    }
}