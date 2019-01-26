#ifndef TOF_INCLUDE_H
#define TOF_INCLUDE_H

#include <cstdlib>
#include <sys/time.h>
#include <stdint.h>
#include <utility>

#include "stddef.h"

typedef enum FRAME_error {
    FRAME_SUCCESS,
    FRAME_ERROR,
} frame_error_t;

#endif // FRAME_INCLUDE_H