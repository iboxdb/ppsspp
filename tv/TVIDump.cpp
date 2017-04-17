#include "stdafx.h"
#include "TVIDump.h"

#include <string>
#include <cstdint>
#include <sstream>


#include "Common/FileUtil.h"
#include "Common/MsgHandler.h"
#include "Common/ColorConv.h"

#include "Core/Config.h" 
#include "Core/System.h"
#include "Core/Screenshot.h"

#include "GPU/Common/GPUDebugInterface.h"

#include "base/timeutil.h"

#include "Core/AVIDump.h"
#include "UI/OnScreenDisplay.h"

AVIDump* test;

static int s_width;
static int s_height;

static GPUDebugBuffer buf;
static u8 *flipbuffer = nullptr;


static u8 *flipbuffer_full = nullptr;
static int32_t flipbuffer_full_pos;
static int32_t flipbuffer_full_pos_max;
static int last_AddFrame;

File::IOFile* iofile = nullptr;

void TVIDump::CheckResolution(int width, int height)
{
	if ((width != s_width || height != s_height) && (width > 0 && height > 0))
	{
		Stop();
	}
}

bool TVIDump::Start(int w, int h)
{
	s_width = w;
	s_height = h;

	std::stringstream s_file_index_str;

	time_update();
	int now = time_now_ms();

	s_file_index_str << time(NULL);
	s_file_index_str << '-';
	s_file_index_str << now;

	char filename[1024];
	snprintf(filename, sizeof(filename), "%s", (GetSysDirectory(DIRECTORY_VIDEO) + "fd" + s_file_index_str.str() + ".tvi").c_str());
	// Make sure that the path exists
	if (!File::Exists(GetSysDirectory(DIRECTORY_VIDEO)))
		File::CreateDir(GetSysDirectory(DIRECTORY_VIDEO));

	if (File::Exists(filename))
		File::Delete(filename);

	iofile = new  File::IOFile(filename, "a");

	//version
	int8_t t = 1;
	iofile->WriteBytes(&t, 1);

	//int size, the time size;
	t = sizeof(int);// == 4
	iofile->WriteBytes(&t, 1);

	t = s_width & 0xFF;
	iofile->WriteBytes(&t, 1);

	t = (s_width >> 8) & 0xFF;
	iofile->WriteBytes(&t, 1);

	t = 0;
	iofile->WriteBytes(&t, 1);
	iofile->WriteBytes(&t, 1);

	t = s_height & 0xFF;
	iofile->WriteBytes(&t, 1);

	t = (s_height >> 8) & 0xFF;
	iofile->WriteBytes(&t, 1);

	t = 0;
	iofile->WriteBytes(&t, 1);
	iofile->WriteBytes(&t, 1);

	//--------------
	t = 0;
	iofile->WriteBytes(&t, 1);
	iofile->WriteBytes(&t, 1);
	iofile->WriteBytes(&t, 1);
	iofile->WriteBytes(&t, 1);

	iofile->Flush();


	flipbuffer_full_pos = 0;
	flipbuffer_full_pos_max = 2 * 60 * 12 * (3 * w * h + 4);
	flipbuffer_full = new u8[flipbuffer_full_pos_max];
	last_AddFrame = -100000;
	return false;
}


void TVIDump::AddFrame()
{
	if (iofile == nullptr) {
		return;
	}
	time_update();
	int now = time_now_ms();
	if (now < last_AddFrame) {
		osm.Show("error last_AddFrame", 3.0f);
		return;
	}
	if (now < (last_AddFrame + 90)) {
		return;
	}
	last_AddFrame = now;

	bool success = gpuDebug->GetCurrentFramebuffer(buf, GPU_DBG_FRAMEBUF_DISPLAY);

	u32 w = buf.GetStride();
	u32 h = buf.GetHeight();
	if (w == 0 || h == 0) {
		//ERROR_LOG(G3D, "Failed to obtain screenshot data.");
		return;
	}
	CheckResolution(w, h);
	if (iofile == nullptr) {
		return;
	}

	const u8 *buffer = ConvertBufferTo888RGB(buf, flipbuffer, w, h);

	if ((flipbuffer_full_pos + (3 * w * h + 4)) > flipbuffer_full_pos_max) {
		osm.Show("error ", 3.0f);
	}

	flipbuffer_full[flipbuffer_full_pos + 0] = now & 0xFF;
	flipbuffer_full[flipbuffer_full_pos + 1] = (now >> 8) & 0xFF;
	flipbuffer_full[flipbuffer_full_pos + 2] = (now >> 16) & 0xFF;
	flipbuffer_full[flipbuffer_full_pos + 3] = (now >> 24) & 0xFF;

	memcpy(flipbuffer_full + flipbuffer_full_pos + 4, buffer, w * h * 3);

	flipbuffer_full_pos += (3 * w * h + 4);

	if (flipbuffer_full_pos >= flipbuffer_full_pos_max) {
		osm.Show("TVI Dump saving. ", 3.0f);
		iofile->WriteBytes(flipbuffer_full, flipbuffer_full_pos);
		Stop();
		delete[] flipbuffer;
		delete[] flipbuffer_full;
		osm.Show("TVI Dump saved.", 3.0f);
	}

}

void TVIDump::Stop()
{
	if (iofile != nullptr) {
		iofile->Flush();
		iofile->Close();
		iofile = nullptr;
	}
}
