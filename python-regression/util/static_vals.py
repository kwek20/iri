from iota import TryteString
NULL_LIST = []

TEST_TRANSACTION_HASH = "BNKODGPOSCN9ENBCFYXPIJZMSACAFTZIAGSWOCZFG9BYECELVD9JLBDSFIDKNXOQIRPTGNWZDMSYZ9999"
TEST_TRANSACTION_HASHES = ["BNKODGPOSCN9ENBCFYXPIJZMSACAFTZIAGSWOCZFG9BYECELVD9JLBDSFIDKNXOQIRPTGNWZDMSYZ9999",
                           "ZPZKTOXRHKRPGNJKOCMHBQWGSMTMSDTVSYHVNZN9MMMPAZHOJYHOESCXGIDLTMXPFWDFKRNHAILRZ9999"]

TEST_NEIGHBORS = [u'tcp://178.128.236.6:14600', u'tcp://167.99.178.3:14600']

TEST_HASH = "NMPXODIWSCTMWRTQ9AI9VROYNFACWCQDXDSJLNC9HKCECBCGQTBUBKVROXZLQWAZRQUGIJTLTMAMSH999"
CONFIRMATION_REFERENCE_HASH = "ZSPRRUJRXHBSRFGCCPVHNXWJJKXRYZSAU9ZEGWFD9LPTWOJZARRLOEQYYWIKOPSXIBFD9ADNIVAHKG999"
NULL_HASH = "999999999999999999999999999999999999999999999999999999999999999999999999999999999"

TEST_TIP_LIST = ["SBKWTQWCFTF9DBZHJKQJKU9LXMZD9BMWJIJLZCCZYJFWIBGYYQBJOWWFWIHDEDTIHUB9PMOWZVCPKV999"]
TEST_HASH_LIST = ["NMPXODIWSCTMWRTQ9AI9VROYNFACWCQDXDSJLNC9HKCECBCGQTBUBKVROXZLQWAZRQUGIJTLTMAMSH999",
                  "IEDSAXC9PQUEHTLDDWMXZXCQYMPRRDAGTBAWHF9VMGQMMVWHYODHVZZMUKXWLIM9WUBOXRKBHBFAWZ999",
                  "INVALIDWSCTMWRTQ9AI9VROYNFACWCQDXDSJLNC9HKCECBCGQTBUBKVROXZLQWAZRQUGIJTLTMAMSH999"]

TEST_ADDRESS = "TEST9TRANSACTION9TEST9TRANSACTION9TEST9TRANSACTION9TEST9TRANSACTION9TEST999999999"
TEST_EMPTY_ADDRESS = "EMPTY9BALANCE9TEST999999999999999999999999999999999999999999999999999999999999999"
TEST_STORE_ADDRESS = "STORE9AND9FIND9999999999999999999999999999999999999999999999999999999999999999999"
TEST_CONSISTENCY_ADDRESS = "THIS9TRANSACTION9IS9NOT9CONSISTENT99999999999999999999999999999999999999999999999"
TEST_BLOWBALL_COO = "EFPNKGPCBXXXLIBYFGIGYBYTFFPIOQVNNVVWTTIYZO9NFREQGVGDQQHUUQ9CLWAEMXVDFSSMOTGAHVIBH"
TEST_TRANSACTIONS_COO = "BTCAAFIH9CJIVIMWFMIHKFNWRTLJRKSTMRCVRE9CIP9AEDTOULVFRHQZT9QAQBZXXAZGBNMVOOKTKAXTB"
THE_BANK = ["THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9999999999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9TWO999999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9THREE9999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9FOUR99999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9FIVE99999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9SIX999999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9SEVEN9999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9EIGHT9999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9NINE99999999999999999999999999999999999999",
            "THIS9TEST9ADDRESS9HAS9ONE9HUNDRED9IOTA9TEN999999999999999999999999999999999999999"]

FAKE_SPEND_ADDRESSES = ["CTCFZQHZ9MVOQVKOASZJFFQYCYSUZOIXFUDGBNQQWNUVNYXJVOYHMQPJVVKNICNRCUDEWXJIEDKXCLVWY",
                        "DRDSKHQ9XHRFMXXHTGZPGUIKWVQYSDQDPUTHEUXEROVUTAQDRCJIWLARCTAQHMYAUVNYNVCEBD9QNC9SD"]

DOUBLE_SPEND_SEED = "THIS9DOUBLE9SPEND9ADDRESS9HAS9ONE9THOUSAND9IOTA9999999999999999999999999999999999"
DOUBLE_SPEND_ADDRESSES = ["YIYPLJDLF9MNSCAORRGFNJNDXOFQZXEXTPDD9TROXZCJPY9AWDTIJY9RKIPLUDPFPLKZRP9NKHPKBJAYA", 
                          "DHGTQLJRYMJTHSYGKCAJYMMWHYYM9XKGYMMKYLEUWOQOAMORGTSWMRHVZ9VKPRTDUGUPBKRB9WMQOBRHY"]

SPLIT_BUNDLE_SEED = "THIS9SPLIT9BUNDLE9ADDRESS9HAS9ONE9THOUSAND9IOTA9999999999999999999999999999999999"
SPLIT_TO_ADDRESS = ["9JMLFZINDAQ99YYDWSYDZMADO9PHWTIZOXHKTSNYFMFMFOQVNAWCATWECJBRYRGPHMBZHYAPTQFFEWZKC"]
SPLIT_REST_ADDRESS = "9JMLFZINDAQ99YYDWSYDZMADO9PHWTIZOXHKTSNYFMFMFOQVNAWCATWECJBRYRGPHMBZHYAPTQFFEWZKC"

SIDE_TANGLE_ADDRESS = "SIDE9TANGLE9999999999999999999999999999999999999999999999999999999999999999999999"
STITCHING_ADDRESS = "STITCHING9TRANSACTIONS99999999999999999999999999999999999999999999999999999999999"
REFERENCING_ADDRESS = "REFERENCES9STITCHING9TRANSACTION9999999999999999999999999999999999999999999999999"

ATTACHED_TRANSACTIONS = []
FOUND_TRANSACTIONS = []

SIDE_TANGLE_TRANSACTIONS = [
    'MKSVLU9BBBKOIAMPHKCHWNKXESH9RWOOAPSJUZXPDIQIMBLIEXLNARBNPKNCHPEZSYBXWUVEKVJMXX999',
    'HADZDR9WDCRFTNRPXZTUUSZYXPRHIGVANQQMHVPGIBDDKEFCWVPAXNZUNEKDAOIOYTVPKOLRNNGVSE999',
    'DBXIMZZZNCFQAZDZDCXAGSMIJPO9WPUKUROVOSISVMQMROAYYHASJLAPGPJ9SYMK9RHULLHRFBLUUB999',
    'RXSBDCTGPLNVFBFBRHTASEQRTHR9TNTOWUFWBO9A9BZXPHXJBNVVSVOITUPSFOUPVDTTJJRXJRERFZ999',
    'IUKPQPVBOYGTSHMXQNT9UAWVF99NHMVUMZFLQNDZJCYRFNHMFBKTUYOTYBWXPZSD9DGPYQOFWZATVA999',
    'VAEHQCOQXSQRDHEJFZIIGK9WVIZYMQUNXLXFZKMVLSZMRCZRHZMHACEAZXIOJHTYIZEFFBETHIOMEU999',
    'UPHGXMJYVXUPUVNDDFJJUACAPS9WAAZVNTARMQUKFFNAJQXWCCPQNQIFNLFVHSEYHVJLEEKGYGNTAK999',
    'AHNOASWQBJZPHWTWUNRHOIVGVRCFOMDBQUQKZSILLURGIKKFUVONSOHOXBTARPHIBLZLFKHQDQQHVC999',
    'SAVFJZ9TWUJVJMIMNPLKKSABAQIAMHIEPHDICVWUJR9GAQJHI9JQDGYFKU9ZFNPGXOOKBLHUZR9OIU999'
]

TEST_TRYTES = [TryteString("9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
9999999999999999999999999999999999999999999999999999999999999999999999999BTCAAFIH9CJIVIMWFMIHKFNWRTLJRKSTMRCVRE9CIP9AED\
TOULVFRHQZT9QAQBZXXAZGBNMVOOKTKAXTB999999999999999999999999999OOL999999999999999999999999PZOOHYD99999999999A99999999COG\
FFWFFHESWQEXJPRMXRAXDWZKJKNKEWTRZRZTFCCDXYSOBOLYEEKMHVIBEBTXHNEHLRFFTPZNPPKZTZDBNUFRSPIATLJAIPFAOPHQYUIL9HCCZCPCYMARJRL\
GCHRER9DPZQZKWTFMS9VNCQNU9MREAWURCRFP999RY9WPBQNZJQVWPEA9PLGEYFKO9KDLHUIKBLHAWFMJQHVBADMAECNWOOMJSOSQDXCASDUITIOQTCAFR9\
99OOL999999999999999999999999OCRAFGUKE999999999L99999999IPEHQKQXYFIYKCAATJXKXYLICFF")]

EMPTY_TRANSACTION_TRYTES = "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
9999999999999999999999999999999999999999999999999999999999999999999999999TEST9TRANSACTION9TEST9TRANSACTION9TEST9TRANSAC\
TION9TEST9TRANSACTION9TEST999999999999999999999999999999999999LA9999999999999999999999999PENPEZD99999999999999999999ZKJ\
DCAXDVILDLFAPDQZCKROIQRDKHZZIX9QQ9RQICWYLH9EUCFZUBKWAAREIXSIPLNQBGXAACBZAKCWLC99999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999"


# Local Snapshot Test files
LS_TEST_STATE_ADDRESSES = [
    "UAWQVYYQGEWMXJLQHWQKQZSFZFFZCUREWZFFWNNBNA9HDJHDPNSLWNMSPDWUPUQQLVXZGMWNB9ABXMPND",
    "SENDREMAINDERTOHERE99999999999999999999999999999999999999999999999999999999999999"
    ]


LS_TEST_MILESTONE_HASHES = [
    "SGANDWBKEVRMTMUPMTWTRLZJGAOKATGLEZFNJFAQIWJPVCVYVEWULOIBNIYKDXYRQCSGLUDGIFCVDRKO9",
    "9LNZBOEFPVDSCKCZEYRUWRHTOYGSSKMUYWNOQVZAUXVDXYGKQ9WIIKZKCVVVTJ9JVQKDZKULTYZVDKBV9",
    "YECIXIYMT9TMUXAEXOAEMDBKWMRVQXMLABVI9PLCJKEKWD9OOBPECXSNDZVRFIMDRDHFPHOC9KQB9VNA9",
    "FVLJEBOTGXCJUDBUGMHYZEHPQGLM9P99ZTDDXLMRDJEEOQEPYBKDSIBZKQJKUXEKBHDRKXGCTYCAKUOU9",
    "PQUJXJRUF9HSILXEE9DVLMVDDSNZSMS99ZWV9FDJXFLIYHOZWIRFJDGSGOQPJCTDHYPNYFQHQQ9LDPFW9"
    ]

LS_PRUNED_TRANSACTIONS = [
    "FKGOUSFWFWSPQXPGEVBXQRYFRGFJWBXRYCXJDZTQWQBQKUJOGEY9JCIVYYSHGNFVIJBIXMZIVDUPBUIYA",
    "EVQEKYWYYDSPHYDVPHATRUHBMKANTTIRA9FXXUYSVRBSEMESKWDBJBWBDARWMGBCFWGI9CFOEI9MXGMYX",
    "HVGYYBVCBWKSMQFTFURLQWWRTNBKYXMXQ9QP9SHSNZJQQ9P9HL9IGNWZBTNMOOLV9GVOBPIBEUCXWNFAY",
    "KESYDCSVJ9TJJIOIMHJDMD9BSPWHQVUIKEBSMYLBTUZZTKKFALDTMATCQWAKIQHNHHZLP9NJFZJMFNEMC",
    "TJRXEMZZMORIHQSGOTRUFATRMMQF9NCGVVCKIDJFIMEZNXFGC9HZQQWZXTMIWILGYBBBFZURU99G9YRLY"
]

UNCONFIRMED_TEST_ADDRESS = [
    "OXKSHMEQYDMJMDPJX9FIEJVWOKCROTCBRPCDCCSBPMZ9EEQMHTEDSDKIKAMZTYNYWIIMZBFY9IXBUGTZD"
]

UNCONFIRMED_TEST_SEED = "SEED9SIX9999999999999999999999999999999999999999999999999999999999999999999999999"

LS_SPENT_ADDRESSES = [
    "YSUAAQHHDARLBEFMLQXQV9DPC9VYSBNGHRNNGZXV9YBKUHTRXJYHOKDOWUEQIDMTULV9JJGRWWQRGCWJD",
    "AVBCKSPHFMXQHGVOBHOYTDFIKHDANAABMJUMSGQGLLUJYPBZXKUWOWQNLC9QHXXALFGIKOVGSONOLIOFC",
    "IDFGBKKDXJQEOYJGNGYIQN9UWXIAXAZSONUVWKIGUUROGMQVRSRYPWONCAENROLC9QHSYKMJYZNFCWLKB",
    "EKQAZGXEKDSXIGZUZNHLBPBXMSSLDDWGIC9JQOJU9PBGTPJVQOVPGTK9FTCAWSAKYQUEKQNSICGO9LIZD",
    "AEL9IIVWWVARSRQPXWDZYAQRKIPJQGQBFJSFDDHUQMPHO9OIOGHYBERROROQYGSDTALHRQGQQ9ORMPTCC"
]

LS_SPENT_TRANSACTIONS = [
    "GRGRBDWMEBOJUUXSQCCMYJHBAJXTUMSRJZ9TKIMNNACILXDGRQ9GWTQZZBTEADQTDCAVWEHZNDKTERSF9",
    "GVWTDRJSIXPSNPDVM9MKP9SOSLWYKKZLOTTCZ9JIMZT9GGLNATTXYWMBYH9ETKLSPIQFYRJSOLHGHUOX9",
    "SUNMO9XIHQDOVSZBWEXBRULETDJFVPFHXG9ASBURSJQZBYYHLWRPCJOHLFUJZLMNAQSBCSCIBU9NYDCZ9"
]

NULL_TRANSACTION = "999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999\
999999999999999999999999999999999999999999999999999999999999999999999999999"

NULL_TRANSACTION_LIST = [TryteString(NULL_TRANSACTION), TryteString(NULL_TRANSACTION), TryteString(NULL_TRANSACTION)]