package com.huomiao.service;

import java.io.FileNotFoundException;
import java.util.List;

public interface AutoCutService {
    String autoAll(String videoUrl, String downloadUrl)  ;

    String autoAllListTask(List<String> vUrls);
}
