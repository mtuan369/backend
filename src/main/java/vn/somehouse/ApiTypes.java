package vn.somehouse;
import com.fasterxml.jackson.databind.JsonNode;import jakarta.validation.constraints.*;import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.*;
public final class ApiTypes {
 public record ItemInput(@NotBlank @Pattern(regexp="PROPERTY|PROJECT|ARTICLE|PAGE") String kind,@NotBlank @Size(max=240) @Pattern(regexp="[a-z0-9]+(?:-[a-z0-9]+)*") String slug,@NotBlank @Size(max=500) String titleVi,@Size(max=500) String titleEn,@Size(max=500) String titleRu,@Size(max=2000) String summaryVi,@Size(max=2000) String summaryEn,@Size(max=2000) String summaryRu,@Size(max=300000) String bodyVi,@Size(max=300000) String bodyEn,@Size(max=300000) String bodyRu,@NotNull @Size(max=1000) String coverUrl,@Pattern(regexp="SALE|RENT") String transactionType,@Size(max=30) String propertyType,@Size(max=240) String projectSlug,@NotNull @DecimalMin("0") @DecimalMax("9000000000000000") BigDecimal price,@NotNull @DecimalMin("0") @DecimalMax("1000000") BigDecimal area,@Min(0) @Max(100) int bedrooms,@Size(max=120) String city,@Size(max=120) String direction,@Size(max=120) String furniture,@Size(max=120) String legalStatus,@Size(max=120) String position,boolean published,LocalDateTime publishedAt,JsonNode data,@Min(0) long version) {}
 public record Item(long id,String kind,String slug,String title,String summary,String body,String coverUrl,String transactionType,String propertyType,String projectSlug,BigDecimal price,BigDecimal area,int bedrooms,String city,String direction,String furniture,String legalStatus,String position,JsonNode data,long version,LocalDateTime publishedAt) {}
 public record Page<T>(List<T> items,int page,int size,long total,int totalPages){}
 public record LeadInput(@NotBlank @Size(max=120) String name,@NotBlank @Pattern(regexp="[+0-9(). -]{8,25}") String phone,@Email @Size(max=254) String email,@NotBlank @Pattern(regexp="BUY|RENT|CONSIGN|INQUIRY") String purpose,@NotBlank @Size(max=4000) String message,@Size(max=240) String propertySlug){}
 public record LeadReceipt(String reference,String message){}
 public record LeadStatus(@NotBlank @Pattern(regexp="NEW|CONTACTED|CLOSED") String status,@Min(0) long version){}
 public record SettingInput(@NotNull JsonNode data,@Min(0) long version){}
 public record PasswordInput(@NotBlank String currentPassword,@NotBlank @Size(min=12,max=72) String newPassword){}
}
