package com.microservice.account.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSignerDto {
    private ObjectId _id;

    private String name;

    private String lastName;

    private String dni;
}
