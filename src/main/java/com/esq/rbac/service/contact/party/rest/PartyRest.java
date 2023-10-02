package com.esq.rbac.service.contact.party.rest;

import java.util.*;
import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.base.vo.Count;
import com.esq.rbac.service.contact.location.domain.Location;
import com.esq.rbac.service.contact.party.domain.Party;
import com.esq.rbac.service.contact.party.repository.PartyRepository;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.util.ContactAuditUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/party")
//@ManagedResource(objectName = "com.esq.dispatcher.contacts:type=REST,name=PartyRest")
public class PartyRest extends BaseRest<Party> {

	private static final String PARAM_Q = "q";
	private static final String ID = "id";
	private static final String CODE = "code";
	private static final String NAME = "name";
	private static final String TYPE = "type";
	private static final String UPDATED_TIME = "updatedTime";
	private static final Set<String> FILTER_COLUMNS;
	private static final Set<String> ORDER_COLUMNS;
	private static final Set<String> SEARCH_COLUMNS;
	private final String SOURCE_DISPATCHER = "Dispatcher";
	private final String SOURCE_RBAC = "RBAC";
	//private static final String SOURCE = "rbac";
	private ContactUserRest userRest;
	private PartyRepository partyRepository;

	static {
		FILTER_COLUMNS = new HashSet<String>(Arrays.asList(
				NAME, CODE, TYPE));

		ORDER_COLUMNS = new HashSet<String>(Arrays.asList(
				ID, NAME, TYPE,
				CODE, UPDATED_TIME,PARAM_TENANT_ID));

		SEARCH_COLUMNS = new HashSet<String>(Arrays.asList(
				NAME, CODE, TYPE));
	}

	@Autowired
	public PartyRest(PartyRepository partyRepository) {
		super(Party.class, partyRepository);
		this.partyRepository=partyRepository;
	}


	@Autowired
	public void setUserRest(ContactUserRest userRest) {
		log.trace("setUserRest;");
		this.userRest = userRest;
	}

	@PostMapping(consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
			produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ResponseEntity create(@RequestBody Party party, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
		int result = 0;
		try {
			result = partyRepository.partyNameSearch(party.getName().trim(),party.getTenantId());
		} catch (Exception e1) {
			log.warn("create;exception={}", e1.getMessage());
		}
		if (result != 0) {
			logException("create;exception={}", new RestException(RestErrorMessages.CREATE_PARTY_FAILED,"Failed to create resource"));
			throw new RestException(RestErrorMessages.CREATE_PARTY_FAILED, "Failed to create resource", party.getName().trim());
		}
		if(party.getContacts()!=null){
			for(int i=0;i<party.getContacts().size();i++){
				party.getContacts().get(i).setPartyInternal(party);
			}
		}
		List<Location> tempLocations = new LinkedList<Location>();
		if(party.getLocations()!=null){
			tempLocations=party.getLocations();
			party.setLocations(null);
		}
		ResponseEntity<Party> response= super.create(party);
		Party createdParty=(Party) super.readById(party.getId()).getBody();
		if(party.getContacts()!=null){
			createdParty.setContacts(party.getContacts());
			for(int i=0;i<createdParty.getContacts().size();i++){
				createdParty.getContacts().get(i).setPartyId(createdParty.getId());
			}

		}
		if(tempLocations!=null){
			createdParty.setLocations(tempLocations);
			for(int i=0;i<createdParty.getLocations().size();i++){
				createdParty.getLocations().get(i).setPartyId(createdParty.getId());
			}
		}
		response = super.update(createdParty.getId(), createdParty);
		log.debug("create; response={}",response);
		try {
			userRest.createAuditLog(TargetOperations.PARTY_TARGET_NAME,TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(createdParty, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
		} catch (Exception e) {
			log.warn("create;exception={}",e);
		}

		return response;
	}

	@PutMapping(value = "/{id}", consumes = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
			produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ResponseEntity<Party> update(@PathVariable("id") long id,@RequestBody Party party,
						   @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
		int result = 0;
		Party savedParty = (Party) super.readById(id).getBody();
		int contactResult = 0;
		try {
			contactResult = partyRepository.partyContactSearch(id);
		} catch (Exception e1) {
			log.warn("update;exception={}", e1);
		}
		if (contactResult != 0) {
			if(!(savedParty.getContacts().size()<=party.getContacts().size())){
				logException("update;exception={}", new RestException(RestErrorMessages.UPDATE_PARTY_FAILED,"Failed to update resource"));
				throw new RestException(RestErrorMessages.UPDATE_PARTY_FAILED, "Failed to update resource", party.getName().trim());
			}
		}

		if(!savedParty.getName().trim().equalsIgnoreCase(party.getName().trim())){
			try {
				result = partyRepository.partyNameSearch(party.getName().trim(),party.getTenantId());
			} catch (Exception e1) {
				log.warn("update;exception={}", e1.getMessage());
			}
			if (result != 0) {
				logException("update;exception={}", new RestException(RestErrorMessages.UPDATE_PARTY_FAILED,"Failed to update resource"));
				throw new RestException(RestErrorMessages.UPDATE_PARTY_FAILED, "Failed to update resource", party.getName().trim());
			}
		}
		ResponseEntity<Party> response = null;
		Party newParty = null;
		String source = headers.get("source") != null && !headers.get("source").isEmpty() ? headers.get("source").get(0) : "";
		if(party.getContacts()!=null){
			for(int i=0;i<party.getContacts().size();i++){
				party.getContacts().get(i).setPartyInternal(party);
			}
		}
		if (source.equalsIgnoreCase(SOURCE_DISPATCHER)) {
			if (party.getIsRBACGroup() == true) {
				savedParty.setContacts(party.getContacts());
				savedParty.setLocations(party.getLocations());
				if(savedParty.getContacts()!=null){
					for(int i=0;i<savedParty.getContacts().size();i++){
						savedParty.getContacts().get(i).setPartyInternal(savedParty);
					}
				}
				response = super.update(id, savedParty);
			} else {
				response = super.update(id, party);
			}
		} else if (source.equalsIgnoreCase(SOURCE_RBAC)) {
			party.setContacts(savedParty.getContacts());
			party.setLocations(savedParty.getLocations());
			response = super.update(id, party);
		} else {
			throw new RestException(RestErrorMessages.UPDATE_FAILED,"No Permissions to Update");
		}
		try {
			userRest.createAuditLog(TargetOperations.PARTY_TARGET_NAME,TargetOperations.UPDATE_OPERATION, ContactAuditUtil.compareObject(savedParty, newParty),headers.get("userId").get(0));
		} catch (Exception e) {
			log.warn("update;exception={}", e);
		}
		return response;
	}

	@GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Override
	public ResponseEntity<Object[]> list(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
		try {
			//MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			Map<String, String[]> parameterMap = request.getParameterMap();
			MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

			parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
			if(uriInfo.containsKey("_"))
				uriInfo.remove("_");
			boolean hasFilter = false;
			for (String filterColumn : FILTER_COLUMNS) {
				if (uriInfo.containsKey(filterColumn)) {
					hasFilter = true;
					break;
				}
			}
			String tenantScope= getTenantScope(uriInfo,headers);
			if (uriInfo.containsKey(PARAM_Q)) {
				String q = uriInfo.getFirst(PARAM_Q);
				String asc = getParameterSingle(uriInfo, PARAM_ASCENDING, null);
				String desc = getParameterSingle(uriInfo, PARAM_DESCENDING, null);
				int first = getParameterSingle(uriInfo, PARAM_FIRST, PARAM_FIRST_DEFAULT);
				int max = getParameterSingle(uriInfo, PARAM_MAX, 0);
				PartyRepository partyRepository = (PartyRepository) repository;
				List<Party> result;
				if(tenantScope==null || tenantScope.trim().equals("") || tenantScope.trim().equals("[]")){
					result = partyRepository.fullTextSearch(q, asc, desc, first, max);
				}else{
					result = partyRepository.fullTextSearch(q, asc, desc, first, max,getTenantData(tenantScope));
				}
				Party[] a = new Party[result.size()];
				return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(result.toArray(a));

			} else if (hasFilter == false) {
				String asc = getParameterSingle(uriInfo, PARAM_ASCENDING, null);
				String desc = getParameterSingle(uriInfo, PARAM_DESCENDING, null);
				int first = getParameterSingle(uriInfo, PARAM_FIRST, 0);
				int max = getParameterSingle(uriInfo, PARAM_MAX, 0);
				PartyRepository partyRepository = (PartyRepository) repository;
				List<Party> result;
				if(tenantScope==null || tenantScope.trim().equals("") || tenantScope.trim().equals("[]") ){
					result = partyRepository.list(asc, desc, first, max);
				}else{
					result = partyRepository.list(asc, desc, first, max,getTenantData(tenantScope));
				}
				Party[] a = new Party[result.size()];
				return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(result.toArray(a));
			}
		} catch (Exception e) {
			logException("list", e);
			throw new RestException(RestErrorMessages.LIST_FAILED, "List resource");
		}

		return super.list(request,headers);
	}

	@GetMapping(value = "/list2", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ResponseEntity<Object[]> list2(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
		return list(request,headers);
	}

	@GetMapping(value = "/count", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Override
	public ResponseEntity<Count> count(HttpServletRequest request, @RequestHeader org.springframework.http.HttpHeaders headers) {
		try {
			//MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			Map<String, String[]> parameterMap = request.getParameterMap();
			MultivaluedMap<String,String> uriInfo = new MultivaluedHashMap<>();

			parameterMap.forEach((key,values)->uriInfo.addAll(key,Arrays.asList(values)));
			String tenantScope= getTenantScope(uriInfo,headers);
			if (uriInfo.containsKey(PARAM_Q)) {
				String q = uriInfo.getFirst(PARAM_Q);
				PartyRepository partyRepository = (PartyRepository) repository;
				int result;
				if(tenantScope==null || tenantScope.trim().equals("") || tenantScope.trim().equals("[]") ){
					result = partyRepository.fullTextCount(q);
				}else{
					result = partyRepository.fullTextCount(q,getTenantData(tenantScope));
				}
				Count count = new Count(result);
				return ResponseEntity.ok().cacheControl(BaseRest.getCacheControl()).body(count);
			}
		} catch (Exception e) {
			logException("count", e);
			throw new RestException(RestErrorMessages.COUNT_FAILED, "Count resource");
		}

		return super.count(request,headers);
	}

	@GetMapping(value = "/count2", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ResponseEntity<Count> count2(HttpServletRequest request) {
		return count(request);
	}

	@DeleteMapping("/{id}")
	public synchronized void deleteById(@PathVariable("id") long id,
										@RequestHeader org.springframework.http.HttpHeaders headers) {
		ResponseEntity<Party> res = super.readById(id);
		Party objectParty = (Party) res.getBody();
		PartyRepository partyRepository = (PartyRepository) repository;
		int result = 0;
		try {
			result = partyRepository.partyContactSearch(id);
		} catch (Exception e1) {
			log.warn("deleteById;exception={}", e1);
		}
		if (result != 0) {
			logException("deleteById;exception={}", new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_PARTY,"Failed to delete resource"));
			throw new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_PARTY,"Failed to delete resource");
		}
		String source = headers.get("source") != null && !headers.get("source").isEmpty() ? headers.get("source").get(0) : "";
		if (objectParty.getIsRBACGroup() == false || source.equalsIgnoreCase(SOURCE_RBAC)) {
			try {
				super.deleteById(id);
				userRest.createAuditLog(TargetOperations.PARTY_TARGET_NAME,	TargetOperations.DELETE_OPERATION, ContactAuditUtil.convertToJSON(objectParty,TargetOperations.DELETE_OPERATION),headers.get("userId").get(0));
			} catch (Exception e) {
				log.warn("deleteById;exception={}", e);
			}
		} else {
			logException("deleteById;exception={}", new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_RBAC,"Failed to delete resource"));
			throw new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_RBAC,"Failed to delete resource");
		}
	}

	@Override
	protected Set<String> getFilterColumns() {
		return FILTER_COLUMNS;
	}

	@Override
	protected Set<String> getSearchColumns() {
		return SEARCH_COLUMNS;
	}

	@Override
	protected Set<String> getOrderColumns() {
		return ORDER_COLUMNS;
	}
}
