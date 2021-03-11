package com.thoughtmechanix.licenses.services;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.thoughtmechanix.licenses.clients.OrganizationDiscoveryClient;
import com.thoughtmechanix.licenses.clients.OrganizationFeignClient;
import com.thoughtmechanix.licenses.clients.OrganizationRestTemplateClient;
import com.thoughtmechanix.licenses.config.ServiceConfig;
import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.model.Organization;
import com.thoughtmechanix.licenses.utils.UserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.thoughtmechanix.licenses.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class LicenseService {

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    OrganizationFeignClient organizationFeignClient;

    @Autowired
    OrganizationRestTemplateClient organizationRestClient;

    @Autowired
    OrganizationDiscoveryClient organizationDiscoveryClient;

    @Autowired
    ServiceConfig config;

    private static final Logger logger = LoggerFactory.getLogger(LicenseService.class);

    public License getLicense(String organizationId, String licenseId, String clientType) {
        License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
        Organization org = retrieveOrgInfo(organizationId, clientType);
        return license
                .withOrganizationName(org.getName())
                .withContactName(org.getContactName())
                .withContactEmail(org.getContactEmail())
                .withContactPhone(org.getContactPhone());
    }

    @HystrixCommand(
            fallbackMethod = "buildFallbackLicenseList",
            threadPoolKey = "licenseByOrgThreadPool",
            //Hystrix不使用servlet容器线程池，默认使用Hystrix线程池，指明另外开设线程池
            threadPoolProperties = {
                    @HystrixProperty(name = "coreSize", value = "30"),
                    @HystrixProperty(name = "maxQueueSize", value = "10")
            },
            commandProperties = {
                    @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value = "13000"),//断路器超时时间
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),//用于控制Hystrix考虑将该断路器跳闸之前,在10s之内必须发生的连续调用数量
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),//是在超过circuitBreaker.requestVolumeThreshold值之后在断路器跳闸之前必须达到的调用失败（由于超时、抛出异常或返回HTTP500）百分比
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),//是在断路器跳闸之后，Hystrix允许另一个调用通过以便查看服务是否恢复健康之前Hystrix的休眠时间

                    //以下统计信息
                    @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),//用于控制Hystrix用来监视服务调用问题的窗口大小，其默认值为10000ms（即10s）。
                    @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5")}//控制在定义的滚动窗口中收集统计信息的次数。在这个窗口中，Hystrix在桶（ bucket）中收集度量数据，并检查这些桶中的统计信息，以确定远程资源调用是否失败。
    )
    public List<License> getLicensesByOrg(String organizationId) {
        randomlyRunLong();

        logger.debug("LicenseService.getLicensesByOrg  Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
        return licenseRepository.findByOrganizationId(organizationId);
    }

    public void saveLicense(License license) {
        license.withId(UUID.randomUUID().toString());

        licenseRepository.save(license);

    }

    public void updateLicense(License license) {
        licenseRepository.save(license);
    }

    public void deleteLicense(License license) {
        licenseRepository.delete(license.getLicenseId());
    }

    private List<License> buildFallbackLicenseList(String organizationId) {
        List<License> fallbackList = new ArrayList<>();
        License license = new License()
                .withId("0000000-00-0000")
                .withOrganizationId(organizationId)
                .withProductName(
                        "hystrix fallback: sory no licensing information currently available"
                );

        fallbackList.add(license);
        return fallbackList;
    }

    private Organization retrieveOrgInfo(String organizationId, String clientType) {
        Organization organization = null;

        switch (clientType) {
            case "feign":
                System.out.println("I am using the feign client");
                organization = organizationFeignClient.getOrganization(organizationId);
                break;
            case "rest":
                System.out.println("I am using the rest client");
                organization = organizationRestClient.getOrganization(organizationId);
                break;
            case "discovery":
                System.out.println("I am using the discovery client");
                organization = organizationDiscoveryClient.getOrganization(organizationId);
                break;
            default:
                organization = organizationRestClient.getOrganization(organizationId);
        }

        return organization;
    }

    private void randomlyRunLong() {
        Random rand = new Random();
        int randomNum = rand.nextInt((3 - 1) + 1) + 1;
        if (randomNum == 3) sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(16000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

