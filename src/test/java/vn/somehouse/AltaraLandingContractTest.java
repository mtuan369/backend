package vn.somehouse;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.zip.CRC32;

@WebMvcTest(value=ApiController.class,properties="app.upload-dir=./target/test-uploads")
@Import({SecurityConfig.class,ApiErrors.class,RequestIds.class})
class AltaraLandingContractTest {
 @Autowired MockMvc mvc;
 @MockitoBean CatalogService catalog;
 @MockitoBean SiteService site;
 @MockitoBean JdbcTemplate db;

 @Test void landingCanBeEmbeddedOnlyByTheSameSite()throws Exception{
  mvc.perform(get("/project-pages/altara-residences-quy-nhon/index.html"))
   .andExpect(status().isOk()).andExpect(header().string("X-Frame-Options","SAMEORIGIN"))
   .andExpect(header().string("Content-Security-Policy",org.hamcrest.Matchers.containsString("frame-ancestors 'self'")));
 }
 @Test void landingScriptsAreServedUnderTheSameEmbeddingPolicy()throws Exception{
  mvc.perform(get("/project-pages/altara-residences-quy-nhon/locale.js"))
   .andExpect(status().isOk()).andExpect(header().string("X-Frame-Options","SAMEORIGIN"));
 }
 @Test void existingAdminProtectionIsRetained()throws Exception{
  mvc.perform(get("/api/v1/admin/items")).andExpect(status().isUnauthorized())
   .andExpect(header().string("X-Frame-Options","DENY"));
 }
 @Test void altaraFormCannotSubmitWithoutCsrf()throws Exception{
  mvc.perform(post("/api/v1/leads").header("Idempotency-Key",UUID.randomUUID()).contentType("application/json")
   .content("{\"name\":\"Altara\",\"phone\":\"0972158609\",\"email\":\"\",\"purpose\":\"BUY\",\"message\":\"Altara Residences Quy Nhơn\"}"))
   .andExpect(status().isForbidden());verifyNoInteractions(site);
 }
 @Test void localizedRentalRequestReturnsTheSavedReference()throws Exception{
  when(site.lead(any(),anyString())).thenReturn(new ApiTypes.LeadReceipt("altara-reference","Đã tiếp nhận"));
  mvc.perform(post("/api/v1/leads").with(csrf()).header("Idempotency-Key",UUID.randomUUID()).contentType("application/json")
   .content("{\"name\":\"Клиент\",\"phone\":\"0972158609\",\"email\":\"client@example.invalid\",\"purpose\":\"RENT\",\"message\":\"Altara Residences Quy Nhơn, ru\"}"))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.reference").value("altara-reference"));
 }
 @Test void historicalMigrationChecksumsMatchTheSuppliedDatabaseDump()throws Exception{
  assertEquals(-416122521,checksum("V1__schema.sql"));assertEquals(252093784,checksum("V2__import_oriva_bay_project.sql"));
 }
 private int checksum(String file)throws Exception{
  try(var stream=getClass().getResourceAsStream("/db/migration/"+file)){
   assertNotNull(stream);String text=new String(stream.readAllBytes(),StandardCharsets.UTF_8).replace("\uFEFF","");
   CRC32 crc=new CRC32();for(String line:text.split("\\R"))crc.update(line.getBytes(StandardCharsets.UTF_8));return (int)crc.getValue();
  }
 }
}
