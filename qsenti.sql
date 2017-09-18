--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: analysisschedule; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE analysisschedule (
    dsid integer NOT NULL,
    schedulehr integer,
    schedulemin integer
);


ALTER TABLE public.analysisschedule OWNER TO postgres;

--
-- Name: chartdata; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE chartdata (
    chtid integer NOT NULL,
    datasource integer,
    width integer,
    height integer,
    xaggregator character varying(10),
    xfield character varying(15),
    yaggregator character varying(10),
    yfield character varying(15),
    groupby character varying(10),
    chartname character varying(25),
    color character varying(7),
    charttype integer
);


ALTER TABLE public.chartdata OWNER TO postgres;

--
-- Name: chartdata_chtid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE chartdata_chtid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.chartdata_chtid_seq OWNER TO postgres;

--
-- Name: chartdata_chtid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE chartdata_chtid_seq OWNED BY chartdata.chtid;


--
-- Name: charttopage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE charttopage (
    ctdid integer NOT NULL,
    page integer,
    chart integer
);


ALTER TABLE public.charttopage OWNER TO postgres;

--
-- Name: charttodata_ctdid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE charttodata_ctdid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.charttodata_ctdid_seq OWNER TO postgres;

--
-- Name: charttodata_ctdid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE charttodata_ctdid_seq OWNED BY charttopage.ctdid;


--
-- Name: tbldatasource; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE tbldatasource (
    dsid integer NOT NULL,
    accounttype character varying,
    accounttitle character varying,
    accesskey character varying,
    accesssecret character varying,
    consumerkey character varying,
    consumersecret character varying,
    keywords character varying,
    processid integer,
    filename character varying,
    tablename character varying
);


ALTER TABLE public.tbldatasource OWNER TO postgres;

--
-- Name: datasource_dsid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE datasource_dsid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.datasource_dsid_seq OWNER TO postgres;

--
-- Name: datasource_dsid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE datasource_dsid_seq OWNED BY tbldatasource.dsid;


--
-- Name: filterdata; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE filterdata (
    ftrid integer NOT NULL,
    field character varying(15),
    condition character varying(2),
    parameter character varying(20),
    filteradder character varying(8),
    htmlobject character varying(20),
    chtid integer
);


ALTER TABLE public.filterdata OWNER TO postgres;

--
-- Name: filterdata_ftrid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE filterdata_ftrid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.filterdata_ftrid_seq OWNER TO postgres;

--
-- Name: filterdata_ftrid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE filterdata_ftrid_seq OWNED BY filterdata.ftrid;


--
-- Name: htmlcomponentdata; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE htmlcomponentdata (
    hcdid integer NOT NULL,
    componenttype character varying(20),
    defaultvalue text,
    controlname character varying(20),
    rpgid integer
);


ALTER TABLE public.htmlcomponentdata OWNER TO postgres;

--
-- Name: htmlcomponentdata_hcdid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE htmlcomponentdata_hcdid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.htmlcomponentdata_hcdid_seq OWNER TO postgres;

--
-- Name: htmlcomponentdata_hcdid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE htmlcomponentdata_hcdid_seq OWNED BY htmlcomponentdata.hcdid;


--
-- Name: reportburstingschedule; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE reportburstingschedule (
    rbsid integer NOT NULL,
    hour integer,
    minute integer,
    frequency character varying(12),
    subject text,
    contents text
);


ALTER TABLE public.reportburstingschedule OWNER TO postgres;

--
-- Name: reportburstingschedule_rps_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE reportburstingschedule_rps_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reportburstingschedule_rps_seq OWNER TO postgres;

--
-- Name: reportburstingschedule_rps_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE reportburstingschedule_rps_seq OWNED BY reportburstingschedule.rbsid;


--
-- Name: reportpage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE reportpage (
    rpgid integer NOT NULL,
    pagename character varying(50),
    linktitle character varying(25),
    status integer
);


ALTER TABLE public.reportpage OWNER TO postgres;

--
-- Name: reportpage_rpgid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE reportpage_rpgid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reportpage_rpgid_seq OWNER TO postgres;

--
-- Name: reportpage_rpgid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE reportpage_rpgid_seq OWNED BY reportpage.rpgid;


--
-- Name: reportview; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE reportview (
    vid integer NOT NULL,
    apiid integer,
    linktitle character varying(25),
    filepath text
);


ALTER TABLE public.reportview OWNER TO postgres;

--
-- Name: reportview_vid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE reportview_vid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.reportview_vid_seq OWNER TO postgres;

--
-- Name: reportview_vid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE reportview_vid_seq OWNED BY reportview.vid;


--
-- Name: sentimentresult; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE sentimentresult (
    date character varying(20),
    sentiment character varying(20),
    retweetcount integer,
    sentimentvalue integer
);


ALTER TABLE public.sentimentresult OWNER TO postgres;

--
-- Name: serviceapi; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE serviceapi (
    apiid integer NOT NULL,
    servicename character varying(50),
    serviceqry text
);


ALTER TABLE public.serviceapi OWNER TO postgres;

--
-- Name: serviceapi_apiid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE serviceapi_apiid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.serviceapi_apiid_seq OWNER TO postgres;

--
-- Name: serviceapi_apiid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE serviceapi_apiid_seq OWNED BY serviceapi.apiid;


--
-- Name: tbluser; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE tbluser (
    uid integer NOT NULL,
    username character varying(16),
    password character varying(16),
    grpid integer NOT NULL,
    email character varying(30)
);


ALTER TABLE public.tbluser OWNER TO postgres;

--
-- Name: tbluser_uid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE tbluser_uid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tbluser_uid_seq OWNER TO postgres;

--
-- Name: tbluser_uid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE tbluser_uid_seq OWNED BY tbluser.uid;


--
-- Name: tblusergroup; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE tblusergroup (
    grpid integer NOT NULL,
    groupname character varying
);


ALTER TABLE public.tblusergroup OWNER TO postgres;

--
-- Name: tblusergroup_grpid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE tblusergroup_grpid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tblusergroup_grpid_seq OWNER TO postgres;

--
-- Name: tblusergroup_grpid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE tblusergroup_grpid_seq OWNED BY tblusergroup.grpid;


--
-- Name: userprevilage; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE userprevilage (
    plgid integer NOT NULL,
    groupid text,
    report text,
    reporttype integer
);


ALTER TABLE public.userprevilage OWNER TO postgres;

--
-- Name: userprevilage_plgid_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE userprevilage_plgid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.userprevilage_plgid_seq OWNER TO postgres;

--
-- Name: userprevilage_plgid_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE userprevilage_plgid_seq OWNED BY userprevilage.plgid;


--
-- Name: chtid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY chartdata ALTER COLUMN chtid SET DEFAULT nextval('chartdata_chtid_seq'::regclass);


--
-- Name: ctdid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY charttopage ALTER COLUMN ctdid SET DEFAULT nextval('charttodata_ctdid_seq'::regclass);


--
-- Name: ftrid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY filterdata ALTER COLUMN ftrid SET DEFAULT nextval('filterdata_ftrid_seq'::regclass);


--
-- Name: hcdid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY htmlcomponentdata ALTER COLUMN hcdid SET DEFAULT nextval('htmlcomponentdata_hcdid_seq'::regclass);


--
-- Name: rbsid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY reportburstingschedule ALTER COLUMN rbsid SET DEFAULT nextval('reportburstingschedule_rps_seq'::regclass);


--
-- Name: rpgid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY reportpage ALTER COLUMN rpgid SET DEFAULT nextval('reportpage_rpgid_seq'::regclass);


--
-- Name: vid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY reportview ALTER COLUMN vid SET DEFAULT nextval('reportview_vid_seq'::regclass);


--
-- Name: apiid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY serviceapi ALTER COLUMN apiid SET DEFAULT nextval('serviceapi_apiid_seq'::regclass);


--
-- Name: dsid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tbldatasource ALTER COLUMN dsid SET DEFAULT nextval('datasource_dsid_seq'::regclass);


--
-- Name: uid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tbluser ALTER COLUMN uid SET DEFAULT nextval('tbluser_uid_seq'::regclass);


--
-- Name: grpid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY tblusergroup ALTER COLUMN grpid SET DEFAULT nextval('tblusergroup_grpid_seq'::regclass);


--
-- Name: plgid; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY userprevilage ALTER COLUMN plgid SET DEFAULT nextval('userprevilage_plgid_seq'::regclass);


--
-- Data for Name: analysisschedule; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY analysisschedule (dsid, schedulehr, schedulemin) FROM stdin;
\.


--
-- Data for Name: chartdata; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY chartdata (chtid, datasource, width, height, xaggregator, xfield, yaggregator, yfield, groupby, chartname, color, charttype) FROM stdin;
19	14	500	500	sum	sentimentvalue	none	date	date	Chart1	#3f3f41	1
20	14	300	300	avg	retweetcount	none	date	date	Chart2	#114f69	1
21	14	950	250	sum	sentimentvalue	none	sentiment	sentiment	Chart3	#7a002d	1
22	14	980	200	avg	sentimentvalue	none	date	date	LineChart1	#076395	2
30	17	300	300	sum	sentimentvalue	none	date	date	seleniumBar	#f90017	1
\.


--
-- Name: chartdata_chtid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('chartdata_chtid_seq', 30, true);


--
-- Name: charttodata_ctdid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('charttodata_ctdid_seq', 29, true);


--
-- Data for Name: charttopage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY charttopage (ctdid, page, chart) FROM stdin;
18	23	22
27	22	19
28	22	21
29	22	20
\.


--
-- Name: datasource_dsid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('datasource_dsid_seq', 18, true);


--
-- Data for Name: filterdata; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY filterdata (ftrid, field, condition, parameter, filteradder, htmlobject, chtid) FROM stdin;
17	date	>	date1	NONE	mydate1	19
18	date	>	date1	NONE	mydate1	20
19	sentiment	=	sentiment	NONE	sentiopt	22
\.


--
-- Name: filterdata_ftrid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('filterdata_ftrid_seq', 19, true);


--
-- Data for Name: htmlcomponentdata; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY htmlcomponentdata (hcdid, componenttype, defaultvalue, controlname, rpgid) FROM stdin;
3	Text Box	2015-05-01	mydate1	22
4	Drop Down	Negative,Very negative,Positive,Very positive,Neutral	sentiopt	23
\.


--
-- Name: htmlcomponentdata_hcdid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('htmlcomponentdata_hcdid_seq', 4, true);


--
-- Data for Name: reportburstingschedule; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY reportburstingschedule (rbsid, hour, minute, frequency, subject, contents) FROM stdin;
3	10	12	? * *	sdfasdfsadf	jlljljl
\.


--
-- Name: reportburstingschedule_rps_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('reportburstingschedule_rps_seq', 3, true);


--
-- Data for Name: reportpage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY reportpage (rpgid, pagename, linktitle, status) FROM stdin;
22	Page1	Page with all controls	1
23	SentiLine	SentiLine	1
24	SeleniumReport Page	SeleniumReport	1
\.


--
-- Name: reportpage_rpgid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('reportpage_rpgid_seq', 24, true);


--
-- Data for Name: reportview; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY reportview (vid, apiid, linktitle, filepath) FROM stdin;
15	4	hello	hello.scala.html
23	5	Sample Selenium View	pie.html
25	1	SampleDel	pie-2.html
\.


--
-- Name: reportview_vid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('reportview_vid_seq', 25, true);


--
-- Data for Name: sentimentresult; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY sentimentresult (date, sentiment, retweetcount, sentimentvalue) FROM stdin;
2015-05-12	Positive	972	1175
2015-05-05	Negative	7952	11886
2015-05-12	Very negative	2	3
2015-05-05	Neutral	4718	8799
2015-05-05	Positive	768	805
2015-05-06	Negative	20568	35344
2015-05-06	Neutral	11717	24148
2015-05-06	Positive	6975	7139
2015-05-06	Very negative	66	79
2015-05-06	Very positive	10	11
2015-05-07	Negative	14858	16569
2015-05-07	Neutral	13459	19396
2015-05-07	Positive	1827	2215
2015-05-07	Very negative	0	5
2015-05-07	Very positive	0	1
2015-05-08	Negative	10596	20823
2015-05-08	Neutral	11988	19730
2015-05-08	Positive	7443	7483
2015-05-08	Very negative	0	1
2015-05-11	Negative	6338	13258
2015-05-11	Neutral	4977	7848
2015-05-11	Positive	410	434
2015-05-11	Very positive	3	5
2015-05-12	Negative	8333	17089
2015-05-12	Neutral	18087	28204
2015-04-29	Negative	39197	69256
2015-04-29	Neutral	7844	19459
2015-04-29	Positive	309	372
2015-04-29	Very negative	3	6
2015-04-29	Very positive	0	1
2015-05-03	Negative	4951	5774
2015-05-03	Neutral	251	364
2015-05-03	Positive	98	150
2015-05-04	Negative	48075	118344
2015-05-04	Neutral	10728	35366
2015-05-04	Positive	611	918
2015-05-04	Very negative	93	97
2015-05-04	Very positive	45	48
2015-05-12	Very positive	0	1
2015-05-13	Negative	11559	54879
2015-05-13	Neutral	14237	32582
2015-05-13	Positive	6902	16866
2015-05-13	Very negative	0	1
2015-05-13	Very positive	15	15
2015-05-14	Negative	8020	28651
2015-05-14	Neutral	4383	13872
2015-05-14	Positive	1037	1306
2015-05-14	Very negative	0	3
\.


--
-- Data for Name: serviceapi; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY serviceapi (apiid, servicename, serviceqry) FROM stdin;
1	Sample Service	SELECT * FROM sentimentresult
2	usertable	SELECT * FROM tbluser
3	dateValuePair	select date,sentimentvalue from sentimentresult LIMIT 8
\.


--
-- Name: serviceapi_apiid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('serviceapi_apiid_seq', 6, true);


--
-- Data for Name: tbldatasource; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY tbldatasource (dsid, accounttype, accounttitle, accesskey, accesssecret, consumerkey, consumersecret, keywords, processid, filename, tablename) FROM stdin;
14	twitter	Kejriwal Trend Analyzer	335366089-vVjzkb0BP6YvvO7r3vGvanA6wFuiqNcMsuF1apvN	qUQgIQ99G8qYOmDW9xgLHp8vu8VmlY6oOvmc8jw3zssdF	A9op3GRJaXuhL1TjYEz094iKs	aLcSA4RnVamUFfqvSStLIFdOO2sVN8Y5QeIyeu3tOvtfVAb8hC	Kejriwal, Aravind Kejriwal, Kejrival, AK_AAP, AravindKejriwal	0	ZQxCUdxdjT	kejriwalTable
15	twitter	iphone6	zsdfzdfg	zdfgzdf	fgzdf	zdfgdxfg	iphone6	0	thWo64v0fi	iphonetable
4	twitter	Delhi Election Streamer	335366089-vVjzkb0BP6YvvO7r3vGvanA6wFuiqNcMsuF1apvN	qUQgIQ99G8qYOmDW9xgLHp8vu8VmlY6oOvmc8jw3zssdF	A9op3GRJaXuhL1TjYEz094iKs	aLcSA4RnVamUFfqvSStLIFdOO2sVN8Y5QeIyeu3tOvtfVAb8hC	delhi, AAP, Kerjriwal, Kejrival, Aam Aadmi, election, news, modi, bjp, congress, rahul, gandhi, rahul gandhi, sonia	0	hello	delhitableMain
17	twitter	SeleniumSample2	klasdJKSFAr6asrr23q	dnfvaw3vsdfg342rfgvH	JIDsd0x97324mskgvH	JIYtsdfi7F6A-ADf4fdsaf	Selenium, Testing IDE	0	Zp90m8lvjY	SeleniumSample2
12	twitter	Indian Cricket	335366089-vVjzkb0BP6YvvO7r3vGvanA6wFuiqNcMsuF1apvN	qUQgIQ99G8qYOmDW9xgLHp8vu8VmlY6oOvmc8jw3zssdF	A9op3GRJaXuhL1TjYEz094iKs	aLcSA4RnVamUFfqvSStLIFdOO2sVN8Y5QeIyeu3tOvtfVAb8hC	cricket, CSK, MI, DDD, IPL, KNR, RR, Kerala Strikers, Kolkatha Night Riders, KXIP, Indian Premier League, Dhoni	0	65dQqsUgGz	cricketForTest
\.


--
-- Data for Name: tbluser; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY tbluser (uid, username, password, grpid, email) FROM stdin;
1	qbroot	qbroot	0	levintom104@gmail.com
5	myuser1	myuser1	6	levintom104@gmail.com
6	sabah	sabah	7	levintom104@gmail.com
\.


--
-- Name: tbluser_uid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('tbluser_uid_seq', 13, true);


--
-- Data for Name: tblusergroup; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY tblusergroup (grpid, groupname) FROM stdin;
6	Executive Officers
12	Sales Team
\.


--
-- Name: tblusergroup_grpid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('tblusergroup_grpid_seq', 12, true);


--
-- Data for Name: userprevilage; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY userprevilage (plgid, groupid, report, reporttype) FROM stdin;
5	6	37693cfc748049e45d87b8c7d8b9aacd	2
7	6	9bf31c7ff062936a96d3c8bd1f8f2ff3	1
8	7	37693cfc748049e45d87b8c7d8b9aacd	2
9	7	b6d767d2f8ed5d21a44b0e5886680cb9	2
10	8	1ff1de774005f8da13f42943881c655f	2
\.


--
-- Name: userprevilage_plgid_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('userprevilage_plgid_seq', 15, true);


--
-- Name: analysisschedule_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY analysisschedule
    ADD CONSTRAINT analysisschedule_pkey PRIMARY KEY (dsid);


--
-- Name: reportview_apiid_key; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY reportview
    ADD CONSTRAINT reportview_apiid_key UNIQUE (apiid);


--
-- Name: serviceapi_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY serviceapi
    ADD CONSTRAINT serviceapi_pkey PRIMARY KEY (apiid);


--
-- Name: tblusergroup_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY tblusergroup
    ADD CONSTRAINT tblusergroup_pkey PRIMARY KEY (grpid);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

