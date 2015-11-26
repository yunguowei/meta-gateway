IWINFO_BACKENDS    = $(BACKENDS)
IWINFO_CFLAGS      = $(CFLAGS) -std=gnu99 -fstrict-aliasing -Iinclude

IWINFO_LIB         = libiwinfo.so
IWINFO_LIB_LDFLAGS = $(LDFLAGS) -shared
IWINFO_LIB_LDADD   =
IWINFO_LIB_OBJ     = iwinfo_utils.o iwinfo_wext.o iwinfo_wext_scan.o iwinfo_lib.o

IWINFO_LUA         = iwinfo.so
IWINFO_LUA_LDFLAGS = $(LDFLAGS) -shared
IWINFO_LUA_LDADD   = -L. -liwinfo -llua
IWINFO_LUA_OBJ     = iwinfo_lua.o

IWINFO_CLI         = iwinfo
IWINFO_CLI_LDFLAGS = $(LDFLAGS)
IWINFO_CLI_LDADD   = -L. -liwinfo
IWINFO_CLI_OBJ     = iwinfo_cli.o


ifneq ($(filter wl,$(IWINFO_BACKENDS)),)
	IWINFO_CFLAGS  += -DUSE_WL
	IWINFO_LIB_OBJ += iwinfo_wl.o
endif

ifneq ($(filter madwifi,$(IWINFO_BACKENDS)),)
	IWINFO_CFLAGS  += -DUSE_MADWIFI
	IWINFO_LIB_OBJ += iwinfo_madwifi.o
endif

ifneq ($(filter nl80211,$(IWINFO_BACKENDS)),)
	IWINFO_CFLAGS      += -DUSE_NL80211
	IWINFO_CLI_LDADD   += -lnl-tiny
	IWINFO_LIB_LDADD   += -lnl-tiny
	IWINFO_LIB_OBJ     += iwinfo_nl80211.o
endif


%.o: %.c
	$(CC) $(IWINFO_CFLAGS) $(FPIC) -c -o $@ $<

compile: $(IWINFO_LIB_OBJ) $(IWINFO_LUA_OBJ) $(IWINFO_CLI_OBJ)
	$(CC) $(IWINFO_LIB_LDFLAGS) -o $(IWINFO_LIB) $(IWINFO_LIB_OBJ) $(IWINFO_LIB_LDADD)
	$(CC) $(IWINFO_LUA_LDFLAGS) -o $(IWINFO_LUA) $(IWINFO_LUA_OBJ) $(IWINFO_LUA_LDADD)
	$(CC) $(IWINFO_CLI_LDFLAGS) -o $(IWINFO_CLI) $(IWINFO_CLI_OBJ) $(IWINFO_CLI_LDADD)

clean:
	rm -f *.o $(IWINFO_LIB) $(IWINFO_LUA) $(IWINFO_CLI)
