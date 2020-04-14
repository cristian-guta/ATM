package com.test.demo;


import com.test.demo.dto.OperationDTO;
import com.test.demo.service.OperationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
public class OperationServiceTest {

    @Mock
    private OperationService operationService;

    @Mock
    private File file;

    Principal principal = new Principal() {
        @Override
        public String getName() {
            return "user";
        }
    };

    @Test
    public void getAllOperationsTest() {
        List<OperationDTO> operationDTOList = new ArrayList<>();
        OperationDTO operationDTO1 = new OperationDTO().setType("tip1").setAmount(23423.3);
        OperationDTO operationDTO2 = new OperationDTO().setType("tip2").setAmount(23423.3);
        OperationDTO operationDTO3 = new OperationDTO().setType("tip3").setAmount(23423.3);

        operationDTOList.add(operationDTO1);
        operationDTOList.add(operationDTO2);
        operationDTOList.add(operationDTO3);


        when(operationService.getAllOperations(principal)).thenReturn(operationDTOList);

        operationService.getAllOperations(principal);

        verify(operationService, times(1)).getAllOperations(principal);
        assertEquals(3, operationDTOList.size());
    }

    @Test
    public void test_create_operation_by_checking_existence_of_bill() throws IOException {

        file = new File("results/logs/log.pdf");

        operationService.createOperation(principal, 1, 0, "alt tip", 333.3);
        verify(operationService, times(1)).createOperation(principal, 1, 0, "alt tip", 333.3);
        assertTrue(file.exists());
    }

}