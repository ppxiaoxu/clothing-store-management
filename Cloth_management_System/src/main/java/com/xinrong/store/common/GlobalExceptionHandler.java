package com.xinrong.store.common;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String DEFAULT_ERROR_MESSAGE = "操作失败，请稍后再试";
    private static final Map<String, String> FIELD_NAMES = Map.ofEntries(
            Map.entry("stock", "库存数量"),
            Map.entry("newStock", "库存数量"),
            Map.entry("sortOrder", "显示顺序"),
            Map.entry("costPrice", "进价"),
            Map.entry("salePrice", "售价"),
            Map.entry("purchasePrice", "进货单价"),
            Map.entry("quantity", "数量"),
            Map.entry("name", "名称"),
            Map.entry("nickname", "昵称"),
            Map.entry("phone", "手机号"),
            Map.entry("avatar", "头像")
    );

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Result.error(400, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return Result.error(400, getFieldErrorMessage(e.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return Result.error(400, getFieldErrorMessage(e.getBindingResult().getFieldErrors()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error(400, "图片大小不能超过5MB");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return Result.error(400, parseRequestBodyErrorMessage(e));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public Result<Void> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e) {
        return Result.error(400, "请求参数格式不正确，请检查输入内容");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        return Result.error(400, "数据已存在，请检查是否重复");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return Result.error(400, parseDatabaseErrorMessage(e));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public Result<Void> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
        log.error("Database connection failed", e);
        return Result.error(500, "数据库连接失败，请检查数据库服务或连接配置");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unhandled server exception", e);
        return Result.error(500, DEFAULT_ERROR_MESSAGE);
    }

    private String getFieldErrorMessage(List<FieldError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return "参数错误，请检查输入内容";
        }
        return fieldErrors.stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining("；"));
    }

    private String parseDatabaseErrorMessage(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause() == null ? "" : e.getMostSpecificCause().getMessage();
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("data too long") || lowerMessage.contains("data truncation")) {
            return "输入内容过长，请缩短后再提交";
        }
        if (lowerMessage.contains("out of range")) {
            return "输入的数值超出允许范围，请调整后再提交";
        }
        if (lowerMessage.contains("cannot be null") || lowerMessage.contains("doesn't have a default value")) {
            return "必填项不能为空，请检查输入内容";
        }
        if (lowerMessage.contains("duplicate")) {
            return "数据已存在，请检查是否重复";
        }
        if (lowerMessage.contains("foreign key constraint")) {
            return "关联数据不存在或已被删除，请刷新后重试";
        }
        return "保存失败，请检查输入内容";
    }

    private String parseRequestBodyErrorMessage(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof JsonMappingException jsonMappingException
                && jsonMappingException.getPath() != null
                && !jsonMappingException.getPath().isEmpty()) {
            String fieldName = jsonMappingException.getPath().get(jsonMappingException.getPath().size() - 1).getFieldName();
            String displayName = FIELD_NAMES.getOrDefault(fieldName, fieldName);
            String message = cause.getMessage() == null ? "" : cause.getMessage().toLowerCase();
            if (message.contains("out of range") || message.contains("numeric value")) {
                return displayName + "超出允许范围，请调整后再提交";
            }
            return displayName + "格式不正确，请检查输入内容";
        }
        return "请求参数格式不正确，请检查输入内容";
    }
}
