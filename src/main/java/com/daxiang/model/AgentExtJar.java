package com.daxiang.model;


import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Objects;

/**
 * Created by jiangyitao.
 */
@Getter
@Setter
public class AgentExtJar {
    private String filename;
    private String md5;
    private File file;
    private String downloadUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentExtJar that = (AgentExtJar) o;
        return md5.equals(that.md5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(md5);
    }

    @Override
    public String toString() {
        return filename;
    }
}
