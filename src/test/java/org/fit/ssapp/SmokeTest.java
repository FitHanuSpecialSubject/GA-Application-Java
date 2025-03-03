package org.fit.ssapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.fit.ssapp.controller.HomeController;
import org.fit.ssapp.service.GameTheoryService;
import org.fit.ssapp.service.PsoCompatSmtService;
import org.fit.ssapp.service.StableMatchingOtmService;
import org.fit.ssapp.service.StableMatchingService;
import org.fit.ssapp.service.TripletMatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SmokeTest {
  @Test
  void HealthCheck() throws Exception {
      this._mock
          .perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(content().contentType("text/html;charset=UTF-8"));
  }

  @Autowired
  private MockMvc _mock;

  @MockBean
  private GameTheoryService gameTheoryService;

  @MockBean
  private StableMatchingService stableMatchingSolver;

  @MockBean
  private StableMatchingOtmService otmProblemSolver;

  @MockBean
  private TripletMatchingService tripletMatchingSolver;

  @MockBean
  private PsoCompatSmtService psoCompatSmtService;
}
