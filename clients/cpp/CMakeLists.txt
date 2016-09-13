project(resthub_cpp_lib)
cmake_minimum_required(VERSION 2.8)

file(GLOB H_SRC
    "include/*.h*"
    "src/*.h*"
)
aux_source_directory(src SRC)

add_library(resthub ${SRC} ${H_SRC})

target_include_directories(resthub PUBLIC include)

target_link_libraries(resthub curl)

add_executable(test_resthub src/test_main.cpp)

target_link_libraries(test_resthub resthub)

enable_testing()

add_test( simple_test test_resthub)
