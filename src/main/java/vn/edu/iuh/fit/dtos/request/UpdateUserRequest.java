/**
 * @ (#) UpdateUserRequest.java      4/9/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/9/2025
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateUserRequest implements java.io.Serializable {

    @JsonProperty("display_name")
    private String displayName;
    private String gender;

    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private String avatar="";
}
