#ifndef MOBILE_DEVICE

#pragma once

#include "Common/CommonTypes.h"




class TVIDump
{
private:
 
	static void CheckResolution(int width, int height);
public:  
	static bool Start(int w, int h);
	static void AddFrame();
	static void Stop();
};
#endif