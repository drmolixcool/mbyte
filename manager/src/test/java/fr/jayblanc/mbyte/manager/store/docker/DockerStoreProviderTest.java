/*
 * Copyright (C) 2025 Jerome Blanchard <jayblanc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package fr.jayblanc.mbyte.manager.store.docker;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DockerStoreProviderTest {

    @Test
    void createAppRunsAgainstRealDockerClient() throws Exception {
        DockerStoreProvider provider = new DockerStoreProvider();
        injectConfig(provider, new DockerStoreProviderConfig() {
            @Override
            public String server() {
                return System.getProperty("test.docker.host", "unix:///var/run/docker.sock");
            }

            @Override
            public String image() {
                return "jerome/store:25.1-SNAPSHOT";
            }

            @Override
            public Workdir workdir() {
                return new Workdir() {
                    @Override
                    public String host() {
                        return "/tmp/stores";
                    }

                    @Override
                    public String local() {
                        return "/home/jboss/mbyte/data/stores";
                    }
                };
            }
        });
        invokeInit(provider);

        assertDoesNotThrow(() -> provider.createStore("42", "owner", "My store"));
    }

    private static void injectConfig(DockerStoreProvider provider, DockerStoreProviderConfig config) throws Exception {
        Field field = DockerStoreProvider.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(provider, config);
    }

    private static void invokeInit(DockerStoreProvider provider) throws Exception {
        Method init = DockerStoreProvider.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(provider);
    }
}
