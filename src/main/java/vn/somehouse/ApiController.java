package vn.somehouse;
import static vn.somehouse.ApiTypes.*;import jakarta.validation.Valid;import jakarta.servlet.http.HttpSession;import org.springframework.web.bind.annotation.*;import org.springframework.security.core.Authentication;import org.springframework.security.web.csrf.CsrfToken;import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.jdbc.core.JdbcTemplate;import org.springframework.web.multipart.MultipartFile;import org.springframework.beans.factory.annotation.Value;import org.springframework.http.*;import java.util.*;import java.nio.file.*;import java.io.IOException;
@RestController @RequestMapping("/api/v1") public class ApiController {
 final CatalogService catalog;final SiteService site;final JdbcTemplate db;final PasswordEncoder passwords;final Path uploads;
 ApiController(CatalogService c,SiteService s,JdbcTemplate db,PasswordEncoder p,@Value("${app.upload-dir}") String dir){catalog=c;site=s;this.db=db;passwords=p;uploads=Path.of(dir).toAbsolutePath().normalize();}
 @GetMapping("/settings") public Map<String,Object> settings(){return site.settings();}
 @GetMapping("/properties") public Page<Item> properties(@RequestParam Map<String,String> q){return catalog.list("PROPERTY",q,false);}
 @GetMapping("/properties/{slug}") public Item property(@PathVariable String slug,@RequestParam(defaultValue="vi") String locale){return catalog.detail("PROPERTY",slug,locale);}
 @GetMapping("/projects") public Page<Item> projects(@RequestParam Map<String,String> q){return catalog.list("PROJECT",q,false);}
 @GetMapping("/projects/{slug}") public Item project(@PathVariable String slug,@RequestParam(defaultValue="vi") String locale){return catalog.detail("PROJECT",slug,locale);}
 @GetMapping("/articles") public Page<Item> articles(@RequestParam Map<String,String> q){return catalog.list("ARTICLE",q,false);}
 @GetMapping("/articles/{slug}") public Item article(@PathVariable String slug,@RequestParam(defaultValue="vi") String locale){return catalog.detail("ARTICLE",slug,locale);}
 @GetMapping("/pages/{slug}") public Item page(@PathVariable String slug,@RequestParam(defaultValue="vi") String locale){return catalog.detail("PAGE",slug,locale);}
 @PostMapping("/leads") @ResponseStatus(HttpStatus.CREATED) public LeadReceipt lead(@Valid @RequestBody LeadInput input,@RequestHeader(value="Idempotency-Key",required=false) String key){return site.lead(input,key);}
 @GetMapping("/auth/csrf") public Map<String,String> csrf(CsrfToken token){return Map.of("token",token.getToken(),"headerName",token.getHeaderName());}
 @GetMapping("/auth/me") public Map<String,Object> me(Authentication auth){if(auth==null||!auth.isAuthenticated()||"anonymousUser".equals(auth.getPrincipal()))return Map.of("authenticated",false);return Map.of("authenticated",true,"email",auth.getName(),"role","ADMIN");}
 @PostMapping("/auth/password") public Map<String,Boolean> password(@Valid @RequestBody PasswordInput input,Authentication auth,HttpSession session){String hash=db.queryForObject("SELECT password_hash FROM app_users WHERE email=?",String.class,auth.getName());if(!passwords.matches(input.currentPassword(),hash))throw ApiException.invalid("Mật khẩu hiện tại không đúng.");if(input.newPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw ApiException.invalid("Mật khẩu dài quá 72 byte.");db.update("UPDATE app_users SET password_hash=? WHERE email=?",passwords.encode(input.newPassword()),auth.getName());session.invalidate();return Map.of("success",true);}
 @GetMapping("/admin/items") public Page<Item> adminItems(@RequestParam(defaultValue="PROPERTY") String kind,@RequestParam Map<String,String> q){return catalog.list(kind,q,true);}
 @GetMapping("/admin/items/{id}") public Map<String,Object> adminItem(@PathVariable long id){return catalog.editor(id);}
 @PostMapping("/admin/items") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> create(@Valid @RequestBody ItemInput item){return catalog.editor(catalog.create(item));}
 @PutMapping("/admin/items/{id}") public Map<String,Object> update(@PathVariable long id,@Valid @RequestBody ItemInput item){catalog.update(id,item);return catalog.editor(id);}
 @DeleteMapping("/admin/items/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable long id,@RequestParam long version){catalog.delete(id,version);}
 @PutMapping("/admin/settings") public Map<String,Object> updateSettings(@Valid @RequestBody SettingInput input){site.settings(input);return site.settings();}
 @GetMapping("/admin/leads") public Page<Map<String,Object>> leads(@RequestParam Map<String,String> q){return site.leads(q);}
 @PatchMapping("/admin/leads/{id}") public Map<String,Boolean> leadStatus(@PathVariable long id,@Valid @RequestBody LeadStatus status){site.status(id,status);return Map.of("success",true);}
 @PostMapping(value="/admin/media",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED) public Map<String,String> upload(@RequestParam MultipartFile file)throws IOException{
  if(file.isEmpty()||file.getSize()>5*1024*1024)throw ApiException.invalid("Ảnh phải nhỏ hơn 5 MB.");byte[] bytes=file.getBytes();String ext=null;
  if(bytes.length>12&&bytes[0]==(byte)0xff&&bytes[1]==(byte)0xd8&&bytes[2]==(byte)0xff)ext="jpg";
  if(bytes.length>12&&Arrays.equals(Arrays.copyOfRange(bytes,0,8),new byte[]{(byte)137,80,78,71,13,10,26,10}))ext="png";
  if(bytes.length>12&&new String(bytes,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")&&new String(bytes,8,4,java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP"))ext="webp";
  if(ext==null)throw ApiException.invalid("Chỉ chấp nhận JPEG, PNG hoặc WebP.");Files.createDirectories(uploads);String name=UUID.randomUUID()+"."+ext;Files.write(uploads.resolve(name),bytes,StandardOpenOption.CREATE_NEW);return Map.of("url","/uploads/"+name);
 }
}
