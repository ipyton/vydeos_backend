ScyllaDB mapper do not support stateless paging well. So this is deprecated.

<plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>17</source> <!-- (or higher) -->
                    <target>17</target> <!-- (or higher) -->
                    <annotationProcessorPaths>
                        <path>
                            <groupId>com.scylladb</groupId>
                            <artifactId>java-driver-mapper-processor</artifactId>
                            <version>4.17.0.0</version>
                        </path>
                        <!-- Optional: add this if you want to avoid the SLF4J warning "Failed to load class
                          StaticLoggerBinder, defaulting to no-operation implementation" when compiling. -->
                        <path>
                            <groupId>org.slf4j</groupId>
                            <artifactId>slf4j-nop</artifactId>
                            <version>1.7.26</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>