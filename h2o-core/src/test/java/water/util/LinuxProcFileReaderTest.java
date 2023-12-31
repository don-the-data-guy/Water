package water.util;

import org.junit.Assume;
import org.junit.Test;
import water.H2ORuntime;
import water.TestUtil;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class LinuxProcFileReaderTest {

    private static final String SYS_TEST_DATA =
            "cpu  43559117 24094 1632164 1033740407 245624 29 200080 0 0 0\n"+
                    "cpu0 1630761 1762 62861 31960072 40486 15 10614 0 0 0\n"+
                    "cpu1 1531923 86 62987 32118372 13190 0 6806 0 0 0\n"+
                    "cpu2 1436788 332 66513 32210723 10867 0 6772 0 0 0\n"+
                    "cpu3 1428700 1001 64574 32223156 8751 0 6811 0 0 0\n"+
                    "cpu4 1424410 152 62649 32232602 6552 0 6836 0 0 0\n"+
                    "cpu5 1427172 1478 58744 32233938 5471 0 6708 0 0 0\n"+
                    "cpu6 1418433 348 60957 32241807 5301 0 6639 0 0 0\n"+
                    "cpu7 1404882 182 60640 32258150 3847 0 6632 0 0 0\n"+
                    "cpu8 1485698 3593 67154 32101739 38387 0 9016 0 0 0\n"+
                    "cpu9 1422404 1601 66489 32193865 15133 0 8800 0 0 0\n"+
                    "cpu10 1383939 3386 69151 32233567 11219 0 8719 0 0 0\n"+
                    "cpu11 1376904 3051 65256 32246197 8307 0 8519 0 0 0\n"+
                    "cpu12 1381437 1496 68003 32237894 6966 0 8676 0 0 0\n"+
                    "cpu13 1376250 1527 66598 32247951 7020 0 8554 0 0 0\n"+
                    "cpu14 1364352 1573 65520 32262764 5093 0 8531 0 0 0\n"+
                    "cpu15 1359076 1176 64380 32269336 5219 0 8593 0 0 0\n"+
                    "cpu16 1363844 6 29612 32344252 4830 2 4366 0 0 0\n"+
                    "cpu17 1477797 1019 70211 32190189 6278 0 3731 0 0 0\n"+
                    "cpu18 1285849 30 29219 32428612 3549 0 3557 0 0 0\n"+
                    "cpu19 1272308 0 27306 32445340 2089 0 3541 0 0 0\n"+
                    "cpu20 1326369 5 29152 32386824 2458 0 4416 0 0 0\n"+
                    "cpu21 1320883 28 31886 32384709 2327 1 4869 0 0 0\n"+
                    "cpu22 1259498 1 26954 32458931 2247 0 3511 0 0 0\n"+
                    "cpu23 1279464 0 26694 32439550 1914 0 3571 0 0 0\n"+
                    "cpu24 1229977 19 32308 32471217 4191 0 4732 0 0 0\n"+
                    "cpu25 1329079 92 79253 32324092 5267 0 4821 0 0 0\n"+
                    "cpu26 1225922 30 34837 32475220 4000 0 4711 0 0 0\n"+
                    "cpu27 1261848 56 43928 32397341 3552 0 5625 0 0 0\n"+
                    "cpu28 1226707 20 36281 32463498 3935 4 5943 0 0 0\n"+
                    "cpu29 1379751 19 35593 32317723 2872 4 5913 0 0 0\n"+
                    "cpu30 1247661 0 32636 32455845 2033 0 4775 0 0 0\n"+
                    "cpu31 1219016 10 33804 32484916 2254 0 4756 0 0 0\n"+
                    "intr 840450413 1194 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 55 0 0 0 0 0 0 45 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 593665 88058 57766 41441 62426 61320 39848 39787 522984 116724 99144 95021 113975 99093 78676 78144 0 168858 168858 168858 162 2986764 4720950 3610168 5059579 3251008 2765017 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 00 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0\n"+
                    "ctxt 1506565570\n"+
                    "btime 1385196580\n"+
                    "processes 1226464\n"+
                    "procs_running 21\n"+
                    "procs_blocked 0\n"+
                    "softirq 793917930 0 156954983 77578 492842649 1992553 0 7758971 51856558 228040 82206598\n";

    private static final String PROC_TEST_DATA = 
            "16790 (java) S 1 16789 16789 0 -1 4202496 6714145 0 0 0 4773058 5391 0 0 20 0 110 0 33573283 64362651648 6467228 18446744073709551615 1073741824 1073778376 140734614041280 140734614032416 140242897981768 0 0 3 16800972 18446744073709551615 0 0 17 27 0 0 0 0 0\n";
    
    @Test
    public void testGetProcessCpusAllowed() {
        LinuxProcFileReader reader = spy(LinuxProcFileReader.class);
        when(reader.getProcessCpusAllowedFallback()).thenReturn(42);
        reader.parseProcessStatusFile("Cpus_allowed: CA");
        
        assertEquals(42, reader.getProcessCpusAllowed(false));
        assertEquals(4, reader.getProcessCpusAllowed(true));
    }

    @Test
    public void getProcessCpusAllowedFallback() {
        Assume.assumeTrue(TestUtil.isCI()); // in CI we know we have at least 4 CPU cores
        String oldValue = System.getProperty("sys.ai.h2o.activeProcessorCount");
        try {
            System.setProperty("sys.ai.h2o.activeProcessorCount", "2");
            assertEquals(2, H2ORuntime.getActiveProcessorCount());
            // this shows we don't use H2ORuntime to report the diagnostic #CPUs - we want the actual number
            assertTrue(new LinuxProcFileReader().getProcessCpusAllowedFallback() > 2);
        } finally {
            if (oldValue == null)
                System.clearProperty("sys.ai.h2o.activeProcessorCount");
            else
                System.setProperty("sys.ai.h2o.activeProcessorCount", oldValue);
        }
    }

    @Test
    public void testParse() {
        LinuxProcFileReader lpfr = new LinuxProcFileReader();
        lpfr.parseSystemProcFile(SYS_TEST_DATA);
        lpfr.parseProcessProcFile(PROC_TEST_DATA);
        assertEquals(1033740407L, lpfr.getSystemIdleTicks());
        assertEquals(1078955782L, lpfr.getSystemTotalTicks());
        assertEquals(4778449, lpfr.getProcessTotalTicks());
        assertEquals(6467228, lpfr.getProcessRss());
        assertEquals(32, lpfr.getCpuTicks().length);
    }

}
