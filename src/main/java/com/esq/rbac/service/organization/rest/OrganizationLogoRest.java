package com.esq.rbac.service.organization.rest;

import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.organization.domain.Organization;
import com.esq.rbac.service.organization.organizationlogo.domain.OrganizationLogo;
import com.esq.rbac.service.organization.organizationlogo.service.OrganizationLogoDal;
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal;
import com.esq.rbac.service.util.AuditLogger;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/organizationLogo")
@Tag(name = "/organizationLogo")
@Slf4j
public class OrganizationLogoRest {

    private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private final OrganizationLogoDal organizationLogoDal;
    private final AuditLogger auditLogger;
    private final OrganizationMaintenanceDal organizationMaintenanceDal;
    private final int THREAD_SIZE = 1;
    ExecutorService organizationLogoExecutor = Executors.newFixedThreadPool(THREAD_SIZE);

    public OrganizationLogoRest(OrganizationLogoDal organizationLogoDal, AuditLogService auditLogDal, OrganizationMaintenanceDal organizationMaintenanceDal) {
        log.trace("setOrganizationLogoDal; {}", organizationLogoDal);
        this.organizationLogoDal = organizationLogoDal;
        this.auditLogger = new AuditLogger(auditLogDal);
        this.organizationMaintenanceDal = organizationMaintenanceDal;
    }

    @PostMapping(value = "/{organizationId}", consumes = MediaType.MULTIPART_FORM_DATA)
    @Parameters({@Parameter(name = "userId", description = "loggedInUserId", required = true, schema = @Schema(type = "string"), in = ParameterIn.HEADER),})
    public void upload(@PathVariable("organizationId") long organizationId, @RequestParam("file") MultipartFile multiPart, @RequestHeader HttpHeaders headers) throws Exception {
        // expecting only one body part (one file upload)
        Integer loggedInUserId = Integer.parseInt(headers.get("userId").get(0));
        String contentType = multiPart.getContentType();
//        String fileName = new String(bodyPart.getContentDisposition().getFileName().getBytes(), UTF8_CHARSET);
        String fileName = multiPart.getOriginalFilename();
        log.trace("upload; organizationId={}; contentType={}; fileName={}", organizationId, contentType, fileName);
        OrganizationLogo organizationLogo = new OrganizationLogo();
        organizationLogo.setOrganizationId(organizationId);
        organizationLogo.setContentType(contentType);
//        organizationLogo.setLogo(bodyPart.getEntityAs(byte[].class));
        organizationLogo.setLogo(multiPart.getBytes());
        Organization organization = organizationMaintenanceDal.getById(organizationId);
        organizationLogoDal.set(organizationId, loggedInUserId, organizationLogo, organization.getOrganizationName());
        organizationLogoExecutor.execute(getOrganizationLogoLogRunnable(loggedInUserId, organization.getOrganizationName()));
    }

    public Runnable getOrganizationLogoLogRunnable(final int userId, final String organzationName) {
        return new Runnable() {
            @Override
            public void run() {
                log.debug("run;");
                try {

                    auditLogger.logCreate(userId, organzationName, "Organization", "Update", organizationLogoDal.getObjectChangeSet());
                } catch (IOException e) {

                    e.printStackTrace();
                }
            }
        };

    }

    //    @GET
//    @Path("/{organizationId}")
    @GetMapping("/{organizationId}")
    public Response download(@PathVariable("organizationId") int organizationId) throws Exception {
        log.trace("download; organizationId={}", organizationId);

        OrganizationLogo organizationLogo = organizationLogoDal.get(organizationId);
        if (organizationLogo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        log.trace("download; contentType={}; contentSize={}", organizationLogo.getContentType(), organizationLogo.getLogo().length);
        return Response.ok().type(organizationLogo.getContentType()).entity(organizationLogo.getLogo()).build();
    }
}
