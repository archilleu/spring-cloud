package com.thoughtmechanix.licenses.clients;

import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.repository.OrganizationRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class OrganizationRestTemplateClient {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ServiceConfig serviceConfig;

    @Autowired
    OrganizationRedisRepository organizationRedisRepository;

    public Organization getOrganization(String organizationId){

        Organization org = checkRedisCache(organizationId);
        if(null != org) {
            log.debug("I have successfuly retrieved an organization {} from the redis cache: {}", organizationId, org);
            return org;
        }

        log.debug("unable to locate organizaion from the redis cache: {}", organizationId);

        String url = serviceConfig.getUrl() + "/api/organizationservice/v1/organizations/{organizationId}";
        ResponseEntity<Organization> restExchange =
                restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null, Organization.class, organizationId);

        org = restExchange.getBody();
        if(org != null) {
            cacheOrganizationObject(org);
        }

        return org;
    }

    private Organization checkRedisCache(String organizationId) {
        try {
            return organizationRedisRepository.findOrganization(organizationId);
        } catch (Exception e) {
            log.error("Error encountered while trying to retireve organization {} check Redis Cache.Exception {},",
                    organizationId, e);
            return null;
        }
    }

    private void cacheOrganizationObject(Organization organization) {
        try {
            organizationRedisRepository.saveOrganization(organization);
        } catch (Exception e) {
            log.error("Unable to cache organization {} in redis. exception {}", organization.getId(), e);
        }
    }

}
