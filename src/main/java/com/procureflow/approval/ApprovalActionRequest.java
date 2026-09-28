package com.procureflow.approval;

import jakarta.validation.constraints.Size;

public record ApprovalActionRequest(

        @Size(
                max = 1000,
                message = "Comment cannot exceed 1000 characters"
        )
        String comment

) {
}