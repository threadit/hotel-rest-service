package com.agoda.hotel.service.impl;

import com.agoda.hotel.dto.RateLimitModel;
import com.agoda.hotel.exception.InvalidAceesKey;
import com.agoda.hotel.service.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {
    private Map<String, RateLimitModel> accessKeyStore;
    private Map<String, RateLimitModel> blockedAccessKeyStore;
     private int requesCountPerSec = 1;
     private int limitInSec = 10000;
     private static  final int fixedRate = 10000; //300000;

    @Autowired
    RateLimitServiceImpl(){ }

    @PostConstruct
    public void createIpStoreMap(){
        accessKeyStore = new ConcurrentHashMap<>();
        blockedAccessKeyStore = new ConcurrentHashMap<>();
    }

    @Override
    public void updateIpStoreMap(RateLimitModel rateLimitModel){
        accessKeyStore.put(rateLimitModel.getApiKey(), rateLimitModel);
    }

    @Override
    public Boolean updateAndCheckEligibilityOfRequest(String apiKey){
        if(accessKeyStore.containsKey(apiKey) ){
            RateLimitModel rateLimitModel = accessKeyStore.get(apiKey);
            return checkAndIncrement(rateLimitModel);
        }
        if(blockedAccessKeyStore.containsKey(apiKey)){
            throw new InvalidAceesKey("please try after sometime");
        }
        else
            throw new InvalidAceesKey("access key is not found");
    }

    private Boolean checkAndIncrement(RateLimitModel rateLimitModel) {
        if (rateLimitModel.getLastTry() - System.currentTimeMillis() <= limitInSec
                && rateLimitModel.getCount() >= requesCountPerSec) {
            rateLimitModel.setLastTry(System.currentTimeMillis());
            rateLimitModel.setCount(rateLimitModel.getCount()+1);
            accessKeyStore.remove(rateLimitModel.getApiKey());
            blockedAccessKeyStore.put(rateLimitModel.getApiKey(), rateLimitModel);
            return false;
        } else {
            rateLimitModel.setLastTry(System.currentTimeMillis());
            rateLimitModel.setCount(rateLimitModel.getCount() + 1);
            return true;
        }
    }

    @Scheduled(fixedRate = fixedRate )
    public void enableAcessKey(){
        System.out.println("i m executing to unblock api-keys");
        Collection<String> apiKeySet = blockedAccessKeyStore.keySet();
        apiKeySet.stream().forEach(apiKey ->{
            RateLimitModel temp = blockedAccessKeyStore.get(apiKey);
            temp.setCount(0);
            accessKeyStore.put(apiKey, temp);
            blockedAccessKeyStore.remove(apiKey);
        });


    }
}
