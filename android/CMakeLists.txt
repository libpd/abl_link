cmake_minimum_required(VERSION 3.6)

set(PD_CORE ${CMAKE_CURRENT_LIST_DIR}/../../pd-for-android/PdCore/src/main)

add_library(pd SHARED IMPORTED)
set_target_properties(pd PROPERTIES IMPORTED_LOCATION
  ${PD_CORE}/libs/${ANDROID_ABI}/libpd.so)

include_directories(${PD_CORE}/jni/libpd/pure-data/src)
include_directories(../external/android-ifaddrs)
include_directories(../external/link/include)
include_directories(../external/link/modules/asio-standalone/asio/include)

add_library(abl_link_tilde SHARED
  ../external/android-ifaddrs/ifaddrs.cpp ../external/abl_link_instance.cpp
  ../external/abl_link~.cpp)
target_compile_definitions(abl_link_tilde PRIVATE
  ANDROID=1 LINK_PLATFORM_LINUX=1 PD=1 ABL_LINK_OFFSET_MS=23)
set(CMAKE_CXX_FLAGS "-std=c++11 -Wno-multichar -fexceptions -Werror")
            
target_link_libraries(abl_link_tilde pd)
