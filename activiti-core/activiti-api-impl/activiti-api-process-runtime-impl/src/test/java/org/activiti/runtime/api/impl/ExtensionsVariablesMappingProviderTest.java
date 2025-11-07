/*
 * Copyright 2010-2025 Hyland Software, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.runtime.api.impl;

import static java.util.Arrays.asList;
import static org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext.buildMappingExecutionContext;
import static org.activiti.engine.impl.util.CollectionUtil.map;
import static org.activiti.engine.impl.util.CollectionUtil.singletonMap;
import static org.activiti.runtime.api.impl.ExtensionsVariablesMappingProvider.JSON_PATCH_MAPPING_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.api.runtime.model.impl.ProcessVariablesMap;
import org.activiti.core.el.ActivitiElContext;
import org.activiti.core.el.CustomFunctionProvider;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.bpmn.behavior.MappingExecutionContext;
import org.activiti.engine.impl.persistence.entity.VariableInstanceEntityImpl;
import org.activiti.engine.impl.variable.StringType;
import org.activiti.spring.process.ProcessExtensionService;
import org.activiti.spring.process.model.Extension;
import org.activiti.spring.process.model.Mapping;
import org.activiti.spring.process.model.ProcessExtensionModel;
import org.activiti.spring.process.model.ProcessVariablesMapping;
import org.activiti.spring.process.model.VariableDefinition;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest
public class ExtensionsVariablesMappingProviderTest {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String EXPRESSION_TEST_FILES_PATH = "src/test/resources/expressions/";

    private static final String JSONPATCH_TEST_FILES_PATH = "src/test/resources/jsonPatch/";

    @InjectMocks
    @Autowired
    private ExtensionsVariablesMappingProvider variablesMappingProvider;

    @Mock
    private ProcessExtensionService processExtensionService;

    @Test
    public void error() throws IOException {
        ProcessVariablesMap availableVariables = new ProcessVariablesMap();

        availableVariables.put("contentText", "%PDF-1.3\n" +
            "%���������\n" +
            "3 0 obj\n" +
            "<< /Filter /FlateDecode /Length 78 >>\n" +
            "stream\n" +
            "x\u0001+T\bT(T0\u0000BSKS\u0005\u000B\u0013#��T�p�<\u0005��Ԣ�Ԃ���\u001C��L�\u001ASc\u0013�:c\u0003\u0005c\u0003=S\u0005CS3��\\\u0005}�\\C\u0005�|�@�@\u0000�\u0012�\n" +
            "endstream\n" +
            "endobj\n" +
            "1 0 obj\n" +
            "<< /Type /Page /Parent 2 0 R /Resources 4 0 R /Contents 3 0 R /MediaBox [0 0 595 842]\n" +
            ">>\n" +
            "endobj\n" +
            "4 0 obj\n" +
            "<< /ProcSet [ /PDF /ImageB /ImageC /ImageI ] /XObject << /Im1 5 0 R >> >>\n" +
            "endobj\n" +
            "5 0 obj\n" +
            "<< /Type /XObject /Subtype /Image /Width 534 /Height 530 /Interpolate true\n" +
            "/ColorSpace 6 0 R /Intent /Perceptual /BitsPerComponent 8 /Length 9819 /Filter\n" +
            "/FlateDecode >>\n" +
            "stream\n" +
            "x\u0001��ٳ�w�\u001F�\u007F!W��U���E�R��j&IMe���d\u0012(\n" +
            "*�Ȁ�ƅ1��\u0001\n" +
            "\f\u0006�c\u001C(\u0018V����ݦ\f^�1F��e˲(��.Y�e\u001B/�Q�\u00130N�G?���>::2�Q��|�N�����t?������}�Hw��B�\u0000\u0001\u0002\u0004\b\u0010 @�\u0000\u0001\u0002\u0004\b\u0010 @�\u0000\u0001\u0002\u0004\b\u0010X�\u0002?���_x���B�\u0000\u0001\u0002\u0004�\u0015X!\u0017�\u007F��c��\u0016\u0001\u0002\u0004\b\u0010����\tr�ȑ���5\u0002\u0004\b\u0010 ���b���\u0017�X\uE3B6\u0011 @�\u0000�c\u0004�\u0012D|\u001C��\u0006\u0001\u0002\u0004\b\u001C_`6A\u001C�:���\u0010 @����l��\u007F�m\u0002\u0004\b\u0010 p|�)A|���H�C�\u0000\u0001\u0002�\bL\t���l\"@�\u0000\u0001\u0002�\u0017\u0018\t�_\u001B<�]|�\u0000\u0001\u0002\u0004\b,#0\u0012�o�/Cc\u0013\u0001\u0002\u0004\b�(0\u0012dŻ�&\u0001\u0002\u0004\b\u0010XF@�,�b\u0013\u0001\u0002g���.�\u0011�\u001E�\u0004�\u0016V�\u0000�\"�㬚6/#P4\u0002\tR\u0004�,\u0001\u0002u\u0002�,�/��k�\u0019�e��|\"�I�|R˅\u0000\u0001\u0002���s�^�=��̺�\u001C���Zϭ<ē�\u0018\t����\u0012 pZ\t̮��R9���.G\u0005&��h�m��J�\u0015p|�\u0000�3Z`v\u0019\u001Ck�X*��\u001B?[7��\t\f�a5�w���\u00049���\u0004\b��\u0002�\u000B`����\u0018q���/O6�\u001C�˧��l�L92k��A�,�b#\u0001\u0002g����-�G�̱�>q��\u007Ff|�^���\b˔#�!���A�,�b#\u0001\u0002g��l|�\u0004\u0019kc\u0016ɬ�#/\u000E\u001F>|����:�\f�P\f��L9�\u0010�;����� �&�\u0010 p�\u000B�\u0004\u0019\u000B`V�\u0011\u001F9(�\u0015rӦ�\u001F��g��J_s\u0002a\tN�\u00025��Bd�]!A\u0016Ml!@��\u0016��\u0001������1�l�9'\u0010��\u0010�H��\u0018\u0012d\u000E�M\u0002\u0004�t��\u001D�\u001C��Q�\u001C��ȇ�}�`�+D�\n" +
            "�8���n�\u00049���x�\u0004\b�\n" +
            "L?-O�?�\u0012�L\u000E��߿\u007F��m7�\u0015\bT��6\u001B\"\u0013���O��\u001Fپ}���װ��?��K/x�E',����\u007F�_��?y��ι��\u0007\u007FpǏ�]����\u007FxEn���kr�\u0015�n��W^s��ϲ��W~��\u0012�-0-t�\t2v@v�\u07BD�i�@�\u0016wC&����)N����{\u007F������A�;�2���{ɻi���i�\t����\u000B����;�����\u000B��\u001F���_��o_y݅\u0017}$����������\u001Fm8����\u000E�O�ޯ��{S�o~�꼒�Κ�,^�}��wm!�n\u0005�B7{\u000E=\u001FU}����s�����J7�\u0015\bT��\u0016�,�+\u001C�:�\tr�u7em�\u05ED?�sz��ٳ/[���7��ݜ\uED9A+�I�x$ž�寏�G�<�c�\\?�k���]u͍��_�۩���\u001Fںm���ه/{\u0007\u001B\t�O��\u0004�q��\u0017�O�{��ݶm۲\u000B��s\u0002�\n" +
            "W�B\u0017��'A^��7���w��Y��췎����=��3\t�\u001D�\\��W��ܰ�����wqv\u0013���Ӎ7�<�����\u001B��?�f�Լ�\u0017g�H�\u000F\\��\u007F�O�M\u001Er�G/}������rߦ�\u007F����������<�\u0003��|�S��o\u007Fqn�5�&���\u007Fzu���?��\u007Fr�l�q=O�W��מ����\u001F���7~?G�r\u001C,7�=�\u001D�<�ce߹���x��?���rsz����UyH^R^O�\u0012p�\u0005˕�Y�q�w^��ss�l��W�1�O\u000F\u001F7�v���D)�7�7e���?\t�+�)AƧ��\u0004ɑ��[��-�n.+\u0010�q k$ȴ\u001B2lg�N�r\u001Fd��Y����Yls�����=���/�ln��ͷޱu�ó7\u000F\u001D:��dA�*z�-�'A��獑�?��H\u001E�E��_�Z��+��>\u001B� ٘�J�|������i����xQ�\u007F�×��\u000E\u001E<��\u000B/����ήɏ<�#�y�\u007F=+�\u007F�.�\u0013=���%���Y�\u0013\u0001��O_��Y�sz岯�m���\u0019w��i�ϖ��E\u0017\u007F<W������ן��<$�g욥�<j<|z�짼���f{�f\u001F��ߺ��lO����\u001F��+C�\u0018\u001A\u000F�'��&�� 9\tr���]�vmٲe�\u0005��9�@�+hs����� \u001F�ħ�\u001E�\u0010M~s>�mV������?�\u0002�w�KG��nξ�7��`���\n" +
            "\u001B�1+\u007F�\u001F>���\u001DF�L��8�)��C�?���<0����e\u0013${=y��LD\u0002+דGsu�ˈ�l��Gj����\u001C!8��d��\t��'Sƹ��\u0014s\t2���Cr-w�\u0002(��2�˫��x�,���O\u0002�J`1Ar<?��Ν;\u001F|������e\u0005\u0002\u0015���n��X�o�S�\u000F��t,��~�K�ʪ��.��E��ͼ\u001Fn����Jd�\"?`gA��<0��\u0016�!\t��WS��ј]~���$H�'5r�k|�I�e*;����\u0014���dT�3�R�\u0017r3'���\\�\u001D\u0013�m\u007Fu����!\u001A\u0007��\u0004\tl�\"/f��\u001C[��K��秇�B`]\tH�eC�6��\tr�m?�ʖ�/Kz������{��/�E��͋?��3{\n" +
            "9̕ϸ��H���X���8\u000F2�}y�\t��y�9\u001E5}͞\u007F\u0019��h\u007F쓟\u0019�G��\u0000ݸy�����\\L���\u0013LsG���A���=s@oD� ١�s%^���+˞�\u0019�͟\u0004z\u000B�� �7o>��t��_���?�^~\u001B1P��>H~x��eȡ�������\u001A8>\u0010������-\u0007��{>';�\u0004�Z��Y?g�:�U���m�J�Cd�O��L�D��?�t�#��[���b��S��>�s�(�t�,�J\u0004�ܜ\u0012$\u001B�)9\u00113=�\u0015\u0002�Y�\"A���{B��\u0013O��\u0010y�9W���k�t�5��Tq�;ߺ�Gwn�؇o�\u001D��n\t�\u000FDe���eW̾��s��5�\u001A�K|�n~\u00190��\u0017o�L��m\u000F?�\u0013\u0379�{�}��\u007FxW��DvN�$t��S�U��X)��Y��v��\u001C���f_|��2Ar�-+|vO��Yo|{�.&�\u0017���lO\u0017ٗ��\u007F.A�lɷ��_\u001D���\tr駿��9w���9/\u001F�HνZ7\t�\u0013��\u0004\u0019�\f�C�;v�xy� #>���\b�s��C�~��?���/x�\n" +
            "�k�;�q�G�]�\u0017�������ߴ�\u0017��/o<�=W�C��5\u0383��JȰ�}#��� c�̧�g�:+jֽ|�(\u001Bǧ�rs\u001C,�����9}�o%n�Tf5\u001E\t�G}�˳1���8����졭���y��T�?����;>7�����>�Fa>f���Q<�2=���l���g\u001F��Q��-�m6�}oΡ�:�MƜ��\u001EG��>]\u0012v���ݢ�\u000F\t��x�<K>��]�Q!G�>�9�ه�9�7�r�\u0010%n���I`�\t�m���ǐ<�\u0010��\u0013?�\u0003\u000F\u001E\\:+z����.�\u000Fmٟ�+o���\t��\u0004�J�\u0004Y�{8\u001F�ݷo�t�ٛ�4r���Ѧ�\u000E�J6f��˦����)�\u001F���\u007Fl٧>���X�8-�£���q�)��?v`��\\�|7����綻I`]\t�a�|�#�?���\u001D;~�w�駟}䑃�7���7�p=\u001Fw��\u001D�����$\u007F>���ؘ#Z7\u007F����ܳ����x�������>�}��7<�kgV�g�2��j��o�vTx��W���]K�ں��˿��M#Ar}�}{�\u001Cy���w'���O���m\u001Fd]��\u05F6�)Aֶ�j\u0004�\n" +
            "�a��U����0Ye�>��8g@�_m<��so9�\u0004A���K�w��G\u001FY:&��3�=���|]����m�cs�l<x��\n" +
            "\u001Bv���$�x\u0001#nR!/)���on��� ��\u001D��\u000F/\u001D\u0003���Cg��+�)AB��\"Az�Q\u0017�L�J��~z��\u000Fܿ/+���.�;\u0015W_����.z��?�1{\u0014kqK\u001E��\u0019��(�ӟ\u001E�ͷ����U�%\u001Bs3Y3Nӏ\u0004ɞ�[�|�y�^�\u007F�$����;G���)A�p[\\�\u001B�\u001F|�E+� p*\u0004N�\u0004ٸa��!���m9c~�7���={\u001E\u001F+�b^,nɡ��}��{\u007F�'�/c\u001F���'��TK�\u001C��i��\\\u0018\tr��\u000F��?�z w���7��g5�%H�\\\b\u0010X�\u0002k� �y~�\u0003���\u001D�?��\u0015x�'�,r\u0010iq\u0004\u001F����,���q�j��'r6d�\u008C\u0004��'�L�|�k�Ō\u0004ɯ���9S��I��\u0011�B�\u0000�e\u0005�<A�V����ʗ�~\u0005�С�~p���k�\n" +
            "��w������g�(V�w�}�K�i��dG���,\u001D���+w��H�w��K�(�\u0007�w�x=�`�H��Ӽ\u0012$b.\u0004\b\u0010X��\u001A&H�_���_9$u�(�\u007F�\u07BC��>L����lȖ�\u0019?�����2�e�HW�(�\u0017���G;r�-\u000F���-I���\u001E=���9���9ӑ�'h�Α\u0004��\u000B\u0001\u0002\u0004^��\u001A&�'?���<����/|��)\u001D�\\��\u001B�I�t�\u000F�6}�-�]��3Kǵ>��;�1\u001F�\u001A�l�s\u001C���s[.���;�/}���'���\u00176�S�S���wo�=�\"���o�H�����@\u0002\u0004\bD`\n" +
            "\u0013dZ�\u000B��u��ο���\u000B[�ɫc�����ze�\u001BϾ��.�q�|�����Τ��D���\u00148�\u0012d&\u000EV��W�S��Ͽ;�&@`.A��[L�?H\u0016�굷G�@�n�6�76\u0001\u0002\u0004N��\u0004��SL���7�� @�4\u0014X!A�\u001F��\f���\u007F�:\n" +
            "�i^\u0012\u0001\u0002�\u0004$�*cb��I�~\u007F/tD��j\u0004$�\n" +
            "Ѱ�oI�ռ�܇\u0000�~\u0002�\tr�ȑ����v�ڲe�*��u~�@�+h��G\u0011��k�\u007F��\u007Fr1lg�3��\u007F��}F�\t\u0010 P'0%HV��{Y\u0000%��\u0006�\\���� uoZ�\t\u00108M\u0004�\u0012$??�\u0004ٽ{�C\u000F��\u001F\u00189�Eu]�?P�\u001A� �\u000E�\u00049M��^\u0006\u0001\u0002u\u0002#A���\u0007�\u0002��\u0013Od1ܻw�֭[/����|]��*�\n" +
            "Q��\u0015��M\t2����Q�Y\n" +
            "�\t\u00108�\u0005��nJ�'�|����Y\u0012�m�v�w���[_+\b�(P�\n" +
            "Z�$ș�7��'@�\u0004F�$Ar�\u0002��SO�����۷}������k~��w���������o�%8!\n" +
            "T��\u0016�\u0000\u000Eɡ:7\b� s n\u0012 p�\u000B��d\u0001\u001C'�\u000F\u001C8�c�9>s�=��y睷�r�M7�t�\n" +
            "7\\w�r���֗�F�\u001D�\u0007!\u0014\u0001\tKpB\u0014�pM\u001F�J�L�s�\n" +
            "\t2\u0007�&\u0001\u0002\n" +
            "\u0004�ݐ|�(!��1�\u000E\u001D\u001A�!9M|��nذ!\u000B���~�o.����o��-\b�\bHX�3v@µx\bk�!A\u0016Ml!@�L\u0017�~fN��2}�7���B�����O�6mʚ���\uF7B9l\\\u0007��v�N�A\bE@�\u0012�q\u000E}�\f�\ts�]!A\u0016Ml!@�����e\u0019�Ά��E�\u0013ɯ�=��\u000F?�pN\u0019g�\\� �\" a\tN��3 ��\u0018�}KH�eYl$@�L\u0017���\u001C�\u001F�!9/�\u001F�s|��\u001Eۿ\u007F\u007FV�={��\u007FV�e/�\u0017��\\�m0\u001B�~\u0010B\u0011��\u0004'D��^[�V8\u00032�\u001E\u0012�L�k��\u0013 p<��\u0010�n��\u0013��������f���۹��d\t]'��t|`\b�\" a\t���U�N\u0018\u001Fa�H�����\u0004\b\u0010(\u0012x�7�_���/��¯~��_���������_G/�s\u001D_�@(�IpB\u0014�p�F��5\u001C�]G/kXP)\u0002\u0004\b�\n" +
            "L+aV�\\F��(ɂ�\u007F��%\b�\u0004$��\u001DQ���v4\u0012dm=U#@�\u0014\bL�a�\u001C����$\u000B��\u0010�Xf��|4\u0012d�I\u0015$@��\b̮���,P4\u0011\tR\u0004�,\u0001\u0002�L`��s=\u007F�z\u0004\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000BH�ja�\t\u0010 �U@�t���\b\u0010 P- A���'@�@W\u0001\t�u��\"@�@��\u0004�\u0016V�\u0000\u0001\u0002]\u0005$H���\u0000\u0001\u0002�\u0002\u0012�ZX}\u0002\u0004\bt\u0015� ]'�/\u0002\u0004\bT\u000B�?\u0013��c\n" +
            "endstream\n" +
            "endobj\n" +
            "7 0 obj\n" +
            "<< /N 3 /Alternate /DeviceRGB /Length 2754 /Filter /FlateDecode >>\n" +
            "stream\n" +
            "x\u0001��gXSI\u0017������\u0012��PC��\u0002H\t=��t�\u0011�@B\t1!��\u0011q\u0005W\u0014\u0015\u0011T\u0016t\u0005D��\u0000�\u0016D\u0011Q\u0016\u0001��\u000B����b��彁M�w?�\u001F�y������9sfn&Ϲ\u0000��l�(\u0003V\u0005 S�-�\f���'$�pc\u0000\u000Ft\u0001\u0011`\u0000�͑��\u0011\u0011s\u0001R佌g���\u0000��n��|���\u0013�qy\u0012\u000E\u0000P\u0004b�̕p2\u0011>��q�H�\n" +
            "\u0000� ��,�\u0016��\n" +
            "�T1\u0012 \u00832N��q\u0019'O1\u001A=e\u0013\u001D�\u000F\u0000Z\u001B\u0000<��\u0016�\u0002@2EtZ\u000E'\u0015�C\n" +
            "@�^�\u0015\b\u0011F��;33���I�-\u0010\u001B\u0011�2���\u007F�I�\u0007���\n" +
            "�lv������D\u0016\u000E\u0010HD\u0019�\u0015S��g��!E�k�Б��\u0017�D�z���H�\n" +
            "S�0y~�\\\u0017 ��3_\u001A\u0012#g��\u001F9�鹒�(����09\u000B3������\u0014A��F�͊��<I`���Y��uS��L��\u0016#��\u001Fiz�B��X\n" +
            "�y��8�M� v��%�Q�x���\n" +
            "],�T�'\f��ۧ��\u0014�)AԿ��\u000BX�����\u0010��a���\u00132g|J�\u0015�qy\u0001�r{�0F�_Q��b-Q��=�:+^F�B��D)�f��\u0015�(;Bq�i�P�\u001D��\u000B\u0002A,p\u0004\u0380\u0001�y���(��\u0012�\u0010\u000BR��4&r�x4��c;��h��\u0002����l\u0000x{w�.B��\u0019��\u0007�K\t�}FK�\n" +
            "@k<\u0000*u3\u001A=\u0017\u0000u\u001D\u0000::9RqΔ;��u\u0018�@\u0005P�\u000E0\u0000&�\u0002� ��\u0002O���\u0019\n" +
            "�A4H\u0000K\u0000\u0007�A&\u0010��`\u0015X\u000F\n" +
            "A1�\u0006v�\n" +
            "P\u0005\u000E�:p\u0014\u001C\u0007-�\f�\u0000.�k�\u000F�\u0002\u000F�\u0010\u0018\u0005/�8x\u000F&!\b�Ad�\u0002�@��\u0019d\n" +
            "9B\f�\u001B\n" +
            "��B�P\u0002�\u0004�BBH\n" +
            "��6@�P)T\u0001UC��/�i�\u0002�\n" +
            "�C��ah\fz\u0003}�Q0\t����9l\u00073`&\u001C\u0006GË�Tx\u0019�\u0007\u0017�[�r�\u0006>\u00027�\u0017�k�-x\b~\u0001O�\u0000J\t��2B٠\u0018(\u007FT8*\u0011��\u0012�֠�Pe�\u001AT#�\n" +
            "Յ��\u001AB�D}Bc�\u00144\n" +
            "m��D��c�\u001C�2�\u001A�\u0016t\u0005�\u000E\u074C����\u001EF���a�\u0018=�5�\u0003���cR1�1��2�!�)L'�\u0016f\u0014�\u001E��jb�X7l\b6\u0001��]�݂݇m¶c��#�\t\u001C\u000E����y��ql\\6�\u0010�\u0007w\u0004w\u001E7�\u001B�}�+�\n" +
            "�� |\"^��Ǘ�\u000F���\u0007�O�\u0004U�\u0019��\u0010N�\u0012V\u0010J\b\u0007\tm��Q�$Q�H'z\u0011��i���rb#��8H|���d�䮴@I��N�\\��\u0015�a�O$u�\u0015ɟ��$%m%Ւ�I�Ho�d�9ٗ�H�&o%ד/�\u001F�?*S�m�Y�\\�ʕ���\u0003ʯT\b*f*L�%*y*e*'T���T%������UרV��V��:�FQsP\u000BW�TۢvX�[�:N�\\=P��^�~@���\b\u0005E1��S8�\n" +
            "���N�(\u0015K�SY�4j1�(��:����\u0011���Q�qVcH\u0013�i�����,�<�y[\uDB8D\uDDBE\u0016S���Y�Qk@��,m_m�v�v��-��:4�@�t��:-:\u000FuѺV�\u000Bt������}9�:�s\u0016gVѬ����zVz�z+�\u000E���M�\u001B�\u0007����_�\u007Fi�i�k�f������!���P`����s�\u0006�Iˠ��.�ƍ�B��F�F�F��t�\u0018�|�&�&D\u0013�I��N�\u000E�qSC�y��L\u001BL�\u0011�\u0018f|��f]f\u001F���q��[̟ѵ�,z\u001E��>hA��XfQcq�\u0012kɰL��g�g\u0005[�X�*��[�֮�\u0002�}���1��g\u000Bg�̾cC�a���4�\f�j�εͷm�}egj�h�ݮ�\uE6FD�}��A�\u0007\u000E�\u000E�\u000E�\u000Em\u000Eo\u001C�\u001C9���7��NANk�Z�^;[;��;�u���s��������U���:�f����\u000E�ʈ`la\\qǸ���u?�����#���_�6�鞇=�͡���98g��؋�U�5�M�N���{��ȇ�S����ė�{��)Ӓ��<�|�g�'�;�����\u007F�\u007F{\u0000* 8�(�7P=0&�\"�Q�qPjPC�x�K����\u0010LHX���;,}\u0016�U�\u001A\u000Fu\u000B]\u001Dz)�\u0014\u0016\u0015V\u0011�x��\\�ܶy��y;�\n" +
            "�7�/��\u0012\u000E�Y�;�\u001FF�#�E��\u0000� bA�'�\u000E��\"��(QK�\u000EG����.�~\u0010c\u0011#��U�]\u0014[\u001F�!. �4n(�.~u��\u0004�\u0004ABk\".16�P������\u0016�.rYT���b�����Kt�d,9�Te){�$LR\\��/�pv\n" +
            "{\"���7y�����y������x���)^)�)�R�Rw���}�e��\u0002\u007FA��uZHZUڇ������\u0019q\u0019M��̤��Bua��R�AVnV��ZT(\u001AZ�lײqq���\u0004�,��fS��Gj!�(\u001D��Ω���<v��\\�\\an�\n" +
            "�\u0015�W<�\u000B��y%z%ge�*�U�W\n" +
            "�f��^\u0003�I^ӱ�dm���u����\u0013ק��-�>�4�݆�\n" +
            "m\u0005�\u0005�\n" +
            "F6\u0006ol(T.\u0014\u0017��乩�\u0007�\u000F�\u001Fz7;m\u07B3�[\u0011��j�}qY�-�-W\u007Ft�����[S������߆�&�v{����R�Ҽґ\u001D�v4��,��n��]�e�eU�������疷�1ݳmϗ\n" +
            "~ŭJ�ʦ�z{7�����o`����*����?\t~�[\u001D\\�\\c^Sv\u0000{ �����\u0007�~f�\\\u007FH�P\uD946\uDFF5�ڡ�ȺK�n����\u000E�4�\n" +
            "҆�#���\u001D\n" +
            "8��h�Xݤ�T|\f\u001C�\u001E{�K�/���\u001D�8�8�x����S�SE�P���\u0016~�PkBk����\u001Dm�m�~�����љʳ\u001AgK�\u0011�\u0015��~>��D�����\u000B#\u001DK;\u001E\\��x�҂K��a�W.\u0007]����:\u007F��ʙn���W\u0019W[��^k�q�9���o�z]{���]o�s�k��\u007Fn�g�\u008D�\u001B�o�n^�5�V���w�,�3t�{�ٽ�{���ܟ|�n\u00103X�P�a�#�G5�[��4�:tv8`��q��\u0007#��\u0017\u007FH��2Z��������g��Ό\u0005��=_�|������?�������ɿ|��\u0019�\u001F\u001F}-~��͖�:ok�9�똈�x�>��䇢�:\u001F�>1>u}���tr�\u0017ܗ�_۾�}\u001B������-fO�\u0002(��SR\u0000xS\u000B\u00009\u0001\u0000\n" +
            "�W\u0010\u0017N��S\u0016��w\u0000²�\u0001Y��\u007F�t�=��\u0015��u\u0000�\"U�\u0012��\u0005�\u001CaU�F \u001C�\u000B`''E\u0005�e:W���E\u0000\u001C�0Դ5���Ӧ�δ�y<2����\u001E�\n" +
            "�]V���\u000F��\u0006�\n" +
            "endstream\n" +
            "endobj\n" +
            "6 0 obj\n" +
            "[ /ICCBased 7 0 R ]\n" +
            "endobj\n" +
            "2 0 obj\n" +
            "<< /Type /Pages /MediaBox [0 0 595 842] /Count 1 /Kids [ 1 0 R ] >>\n" +
            "endobj\n" +
            "8 0 obj\n" +
            "<< /Type /Catalog /Pages 2 0 R >>\n" +
            "endobj\n" +
            "9 0 obj\n" +
            "<< /Title (Screenshot 2025-10-13 at 14.13.12) /Producer (macOS Version 15.6.1 \\(Build 24G90\\) Quartz PDFContext)\n" +
            "/Creator (Preview) /CreationDate (D:20251013121425Z00'00') /ModDate (D:20251013121425Z00'00')\n" +
            ">>\n" +
            "endobj\n" +
            "xref\n" +
            "0 10\n" +
            "0000000000 65535 f \n" +
            "0000000171 00000 n \n" +
            "0000013275 00000 n \n" +
            "0000000022 00000 n \n" +
            "0000000275 00000 n \n" +
            "0000000364 00000 n \n" +
            "0000013240 00000 n \n" +
            "0000010386 00000 n \n" +
            "0000013358 00000 n \n" +
            "0000013407 00000 n \n" +
            "trailer\n" +
            "<< /Size 10 /Root 8 0 R /Info 9 0 R /ID [ <d146f1f990aa9ac7fa811ee2dced9e9b>\n" +
            "<d146f1f990aa9ac7fa811ee2dced9e9b> ] >>\n" +
            "startxref\n" +
            "13632\n" +
            "%%EOF\n");


        //given
        ExpressionResolver expressionResolver = mock(ExpressionResolver.class);
        given(expressionResolver.containsExpression(any())).willReturn(false);
        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        ObjectMapper objectMapper = new ObjectMapper();
        ProcessExtensionModel extensions = objectMapper.readValue(
            new File("src/test/resources/task-variable-no-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableNoMapping"));


        Map map = variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution), availableVariables);
    }

    @Test
    public void calculateInputVariablesShouldDoMappingWhenThereIsMappingSet() throws Exception {
        //given
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        Extension processExtensions = extensions.getExtensions("Process_taskVarMapping");
        DelegateExecution execution = buildExecution(processExtensions);
        given(execution.getVariable("process_variable_inputmap_1")).willReturn("new-input-value");
        given(execution.getVariable("property-with-no-default-value")).willReturn(null);

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables.get("task_input_variable_name_1")).isEqualTo("new-input-value");

        //mapped with process variable that is null, so it should not be present
        assertThat(inputVariables).doesNotContainKeys("task_input_variable_mapped_with_null_process_variable");
    }

    private DelegateExecution buildExecution(Extension extensions) {
        return buildExecution(extensions, "simpleTask");
    }

    private DelegateExecution buildExecution(Extension extensions, String taskName) {
        DelegateExecution execution = mock(DelegateExecution.class);
        String processDefinitionId = "procDefId";
        given(execution.getProcessDefinitionId()).willReturn(processDefinitionId);
        given(execution.getCurrentActivityId()).willReturn(taskName);

        given(processExtensionService.getExtensionsForId(processDefinitionId)).willReturn(extensions);
        return execution;
    }

    @Test
    public void calculateInputVariablesShouldPassAllVariablesWhenThereIsNoMapping() throws Exception {
        //given
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-no-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        Extension processExtensions = extensions.getExtensions("Process_taskVariableNoMapping");
        DelegateExecution execution = buildExecution(processExtensions);
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        Map<String, Object> variables = map("var-one", "one", "var-two", 2);

        given(execution.getVariables()).willReturn(variables);

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isEqualTo(variables);
    }

    @Test
    public void calculateInputVariablesShouldNotPassAnyVariablesWhenTheMappingIsEmpty() throws Exception {
        //given
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-empty-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableEmptyMapping"));

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isEmpty();
    }

    @Test
    public void calculateInputVariablesShouldPassOnlyConstantsWhenTheMappingIsEmpty() throws Exception {
        //given
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-empty-mapping-with-constants-extensions.json"),
            ProcessExtensionModel.class
        );

        DelegateExecution execution = buildExecution(
            extensions.getExtensions("Process_taskVariableEmptyMappingWithContants")
        );

        //when
        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        //then
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1_2", "constant_2_value"),
                tuple("process_constant_inputmap_2", "constant_value")
            );
    }

    @Test
    public void calculateOutputVariablesShouldDoMappingWhenThereIsMappingSet() throws Exception {
        //given
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        Extension processExtensions = extensions.getExtensions("Process_taskVarMapping");
        DelegateExecution execution = buildExecution(processExtensions);
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        Map<String, Object> entityVariables = singletonMap("task_output_variable_name_1", "var-one");

        ExpressionResolverHelper.setExecutionVariables(execution, entityVariables);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            entityVariables
        );

        //then
        assertThat(outPutVariables.get("process_variable_outputmap_1")).isEqualTo("var-one");

        //mapped with a task variable that is not present, so it should not be present
        assertThat(outPutVariables).doesNotContainKey("property-with-no-default-value");
    }

    @Test
    public void calculateOutputVariablesShouldPassAllVariablesWhenThereIsNoMapping() throws Exception {
        //given
        ExpressionResolver expressionResolver = mock(ExpressionResolver.class);
        given(expressionResolver.containsExpression(any())).willReturn(false);
        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-no-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableNoMapping"));

        Map<String, Object> taskVariables = map(
            "task_output_variable_name_1",
            "var-one",
            "non-mapped-output_variable_name_2",
            "var-two"
        );

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            taskVariables
        );

        //then
        assertThat(outPutVariables).isEqualTo(taskVariables);
    }

    @Test
    public void calculateOutputVariablesShouldNotPassAnyVariablesWhenTheMappingIsEmpty() throws Exception {
        //given
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-empty-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        DelegateExecution execution = buildExecution(extensions.getExtensions("Process_taskVariableEmptyMapping"));

        Map<String, Object> taskVariables = map(
            "task_output_variable_name_1",
            "var-one",
            "non-mapped-output_variable_name_2",
            "var-two"
        );

        //when
        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            taskVariables
        );

        //then
        assertThat(outputVariables).isEmpty();
    }

    @Test
    public void calculateOutputVariablesShouldConvertValueFromDoubleToBigDecimal() {
        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "bigdecimal-process-variable";
        String doubleOutputName = "double-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition bigdecimalProcessVariable = new VariableDefinition();
        bigdecimalProcessVariable.setType("bigdecimal");
        bigdecimalProcessVariable.setName(processVariableName);
        bigdecimalProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, bigdecimalProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(doubleOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        double doubleValue = 2.3;
        BigDecimal bigDecimalValue = BigDecimal.valueOf(doubleValue);
        Map<String, Object> availableVariables = singletonMap(doubleOutputName, doubleValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            availableVariables
        );

        //then
        assertThat(outPutVariables.get(processVariableName)).isEqualTo(bigDecimalValue);
    }

    @Test
    public void calculateOutputVariablesShouldConvertValueFromIntegerToBigDecimal() {
        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "bigdecimal-process-variable";
        String integerOutputName = "integer-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition bigdecimalProcessVariable = new VariableDefinition();
        bigdecimalProcessVariable.setType("bigdecimal");
        bigdecimalProcessVariable.setName(processVariableName);
        bigdecimalProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, bigdecimalProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(integerOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        Integer intValue = 2;
        BigDecimal bigDecimalValue = BigDecimal.valueOf(intValue);

        Map<String, Object> availableVariables = singletonMap(integerOutputName, intValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            availableVariables
        );

        //then
        assertThat(outPutVariables.get(processVariableName))
            .asInstanceOf(InstanceOfAssertFactories.BIG_DECIMAL)
            .isEqualByComparingTo(bigDecimalValue);
    }

    @Test
    public void calculateOutputVariablesShouldConvertLocalDateToDate() {
        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "localdate-process-variable";
        String integerOutputName = "date-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition localDateProcessVariable = new VariableDefinition();
        localDateProcessVariable.setType("date");
        localDateProcessVariable.setName(processVariableName);
        localDateProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, localDateProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(integerOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        LocalDate localDateValue = LocalDate.now();
        Date dateValue = Date.from(localDateValue.atStartOfDay(ZoneOffset.UTC).toInstant());

        Map<String, Object> availableVariables = singletonMap(integerOutputName, localDateValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            availableVariables
        );

        //then
        assertThat(outPutVariables.get(processVariableName))
            .asInstanceOf(InstanceOfAssertFactories.DATE)
            .isEqualTo(dateValue);
    }

    @Test
    public void calculateOutputVariablesShouldConvertLocalDateTimeToDate() {
        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "localdate-process-variable";
        String integerOutputName = "date-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition localDateProcessVariable = new VariableDefinition();
        localDateProcessVariable.setType("date");
        localDateProcessVariable.setName(processVariableName);
        localDateProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, localDateProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(integerOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        LocalDateTime localDateTimeValue = LocalDateTime.now();
        Date dateValue = Date.from(localDateTimeValue.atZone(ZoneOffset.UTC).toInstant());

        Map<String, Object> availableVariables = singletonMap(integerOutputName, localDateTimeValue);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            availableVariables
        );

        //then
        assertThat(outPutVariables.get(processVariableName))
            .asInstanceOf(InstanceOfAssertFactories.DATE)
            .isEqualTo(dateValue);
    }

    @Test
    public void calculateOutputVariablesShouldConvertValueFromStringToBigDecimal() {
        //given
        String taskId = "task-id";
        String processVariableId = "process-variable-id";
        String processVariableName = "bigdecimal-process-variable";
        String stringOutputName = "string-output";

        Extension extension = new Extension();
        DelegateExecution execution = buildExecution(extension, taskId);

        VariableDefinition bigdecimalProcessVariable = new VariableDefinition();
        bigdecimalProcessVariable.setType("bigdecimal");
        bigdecimalProcessVariable.setName(processVariableName);
        bigdecimalProcessVariable.setId(processVariableId);
        extension.setProperties(Map.of(processVariableId, bigdecimalProcessVariable));

        ProcessVariablesMapping mappings = new ProcessVariablesMapping();
        Mapping mapping = new Mapping();
        mapping.setType(Mapping.SourceMappingType.VARIABLE);
        mapping.setValue(stringOutputName);
        mappings.setOutputs(Map.of(processVariableName, mapping));
        extension.setMappings(Map.of(taskId, mappings));

        String stringValue = "4.1";
        Map<String, Object> availableVariables = singletonMap(stringOutputName, stringValue);

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, extension);
        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);
        ExpressionResolverHelper.setExecutionVariables(execution, availableVariables);

        //when
        Map<String, Object> outPutVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            availableVariables
        );

        //then
        assertThat(outPutVariables.get(processVariableName))
            .asInstanceOf(InstanceOfAssertFactories.BIG_DECIMAL)
            .isEqualByComparingTo(stringValue);
    }

    @Test
    public void should_calculateOutputVariables_when_usingJsonPatchVariablesMapping() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(
            JSONPATCH_TEST_FILES_PATH,
            "jsonPatch-in-mapping-output.json",
            "Process_jsonPatchMappingOutput"
        );

        Map<String, Object> outputVariables = executeCalculateOutputVariables(execution);

        assertOutputVariables(outputVariables);
    }

    @Test
    public void should_calculateOutputVariables_when_jsonPatchOriginalVariableIsEmptyJson() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(
            JSONPATCH_TEST_FILES_PATH,
            "jsonPatch-in-mapping-output.json",
            "Process_jsonPatchMappingOutput"
        );
        when(execution.getVariable(eq("process_variable_empty_json"))).thenReturn(NullNode.getInstance());

        Map<String, Object> outputVariables = executeCalculateOutputVariables(execution);

        assertOutputVariables(outputVariables);
    }

    private Map<String, Object> executeCalculateOutputVariables(DelegateExecution execution) {
        return variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            map(
                "task_input_variable_name_1",
                "variable_value_1",
                "task_input_variable_name_2",
                Map.of("firstname", "Bob")
            )
        );
    }

    private void assertOutputVariables(Map<String, Object> outputVariables) {
        Map<String, Object> expectedAddress0 = Map.of("street", "123 Main St");
        Map<String, Object> expectedAddress1 = Map.of("street", "456 Elm St");
        Map<String, Object> expectedAddress2 = Map.of("street", "Ha-Ha Road", "new-street-field", "Street Name");
        Map<String, Object> expectedAddress3 = Map.of("address", Map.of("street", "Ha-Ha Road"));
        Map<String, Object> expectedAddress5 = Map.of("street", "123 Main St", "propertyFromVariable", "Street Name");
        Map<String, Object> expectedAddress6 = Map.of("street", "456 Elm St", "propertyFromVariable", "Street Name");
        Map<String, Object> expectedAddress7 = Map.of("street", "100 Replaced address");


        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple(
                    "process_variable_person_simple_cases",
                    Map.of(
                        "firstname",
                        "Bob",
                        "lastname",
                        "Miracle",
                        "addresses",
                        List.of(expectedAddress0, expectedAddress1)
                    )
                ),
                tuple(
                    "process_variable_empty_json",
                    Map.of("firstname", "John", "address", Map.of("street", "Ha-Ha Road"))
                ),
                tuple("variable_invalid_object", Map.of("street2", "Ha-Ha Road")),
                tuple(
                    "process_variable_person_array_cases",
                    Map.of(
                        "firstname",
                        "Bob",
                        "addresses",
                        List.of(expectedAddress0, expectedAddress2, expectedAddress7, expectedAddress3)
                    )
                ),
                tuple(
                    "process_variable_person_variable_cases",
                    Map.of(
                        "firstname",
                        "Bob",
                        "propertyFromVariable",
                        "Miracle",
                        "process_variable_name_equals_value",
                        "Miracle",
                        "addresses",
                        List.of(expectedAddress5, expectedAddress6, expectedAddress3)
                    )
                ),
                tuple("process_variable_empty_inner_array", Map.of("people", List.of(Map.of("name", "John"))))
            );
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_JsonPatchDefinitionIsInvalid() throws IOException {
        DelegateExecution execution = initExpressionResolverTest(
            JSONPATCH_TEST_FILES_PATH,
            "invalid-jsonPatch-in-mapping-output.json",
            "Process_jsonPatchMappingOutput"
        );
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () ->
            variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution), null)
        );

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_jsonPatchMappingContainsInvalidPathVariableType()
        throws IOException {
        DelegateExecution execution = initExpressionResolverTest(
            JSONPATCH_TEST_FILES_PATH,
            "jsonPatch-invalid-path-variable-type.json",
            "Process_jsonPatchMappingOutput"
        );
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () ->
            variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution), null)
        );

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
        assertThat(
            "Variable process_variable_json of type 'json' is not allowed in JsonPatch mapping. Only string and integer types are allowed"
        ).isEqualTo(exception.getCause().getMessage());
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_jsonPatchMappingContainsEmptyPathVariable()
        throws IOException {
        DelegateExecution execution = initExpressionResolverTest(
            JSONPATCH_TEST_FILES_PATH,
            "jsonPatch-invalid-path-variable-empty.json",
            "Process_jsonPatchMappingOutput"
        );
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () ->
            variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution), null)
        );

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
        assertThat("Path variable $process_variable_empty used in JsonPatch mapping should not be empty").isEqualTo(
            exception.getCause().getMessage()
        );
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_jsonPatchMappingContainsUndefinedPathVariable()
        throws IOException {
        DelegateExecution execution = initExpressionResolverTest(
            JSONPATCH_TEST_FILES_PATH,
            "jsonPatch-invalid-path-variable-undefined.json",
            "Process_jsonPatchMappingOutput"
        );
        ActivitiIllegalArgumentException exception = assertThrows(ActivitiIllegalArgumentException.class, () ->
            variablesMappingProvider.calculateOutPutVariables(buildMappingExecutionContext(execution), null)
        );

        assertThat(JSON_PATCH_MAPPING_ERROR).isEqualTo(exception.getMessage());
        assertThat(
            "Path variable $undefined used in JsonPatch mapping is not defined for the current process"
        ).isEqualTo(exception.getCause().getMessage());
    }

    private DelegateExecution initExpressionResolverTest(String fileName, String processDefinitionKey)
        throws IOException {
        return initExpressionResolverTest(fileName, processDefinitionKey, new ArrayList<>());
    }

    private DelegateExecution initExpressionResolverTest(String filePath, String fileName, String processDefinitionKey)
        throws IOException {
        return initExpressionResolverTest(filePath, fileName, processDefinitionKey, new ArrayList<>());
    }

    private DelegateExecution initExpressionResolverTest(
        String fileName,
        String processDefinitionKey,
        List<CustomFunctionProvider> customFunctionProviders
    ) throws IOException {
        return initExpressionResolverTest(
            EXPRESSION_TEST_FILES_PATH,
            fileName,
            processDefinitionKey,
            customFunctionProviders
        );
    }

    private DelegateExecution initExpressionResolverTest(
        String filePath,
        String fileName,
        String processDefinitionKey,
        List<CustomFunctionProvider> customFunctionProviders
    ) throws IOException {
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File(filePath + fileName),
            ProcessExtensionModel.class
        );

        DelegateExecution execution = buildExecution(extensions.getExtensions(processDefinitionKey));
        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(
            execution,
            extensions.getExtensions(processDefinitionKey),
            customFunctionProviders,
            new ArrayList<>()
        );

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        return execution;
    }

    @Test
    public void should_notSubstituteExpressions_when_thereAreNoExpressions() throws Exception {
        DelegateExecution execution = initExpressionResolverTest("no-expression.json", "Process_NoExpression");

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", "variable_value_1"),
                tuple("task_input_variable_name_2", "static_value_1")
            );

        Map<String, Object> taskVariables = map(
            "task_input_variable_name_1",
            "variable_value_1",
            "task_input_variable_name_2",
            "static_value_2"
        );

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            taskVariables
        );

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_variable_3", "variable_value_1"),
                tuple("process_variable_4", "static_value_2")
            );
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInConstants() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-constants.json",
            "Process_expression-in-constants"
        );

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1", "${process_variable_1}"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", "variable_value_1"),
                tuple("task_input_variable_name_2", "static_value_1")
            );

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            map("task_input_variable_name_1", "variable_value_1", "task_input_variable_name_2", "static_value_2")
        );

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_variable_3", "variable_value_1"),
                tuple("process_variable_4", "static_value_2")
            );
    }

    @Test
    public void should_substituteExpressions_when_expressionIsInInputMappingValue() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-mapping-input-value.json",
            "Process_expressionMappingInputValue"
        );

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", "variable_value_1"),
                tuple("task_input_variable_name_2", "variable_value_1")
            );
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInInputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-mapping-input-variable.json",
            "Process_expressionMappingInputVariable"
        );

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_2", "static_value_1")
            );
    }

    @Test
    public void should_substituteExpressions_when_expressionIsInOutputMappingValue() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue"
        );

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            map("task_input_variable_name_1", "variable_value_1", "task_input_variable_name_2", "static_value_2")
        );

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_variable_3", "variable_value_1"),
                tuple("process_variable_4", "static_value_2")
            );
    }

    @Test
    public void should_notSubstituteExpressions_when_expressionIsInOutputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-mapping-output-variable.json",
            "Process_expressionMappingOutputVariable"
        );

        Map<String, Object> taskVariables = map(
            "task_input_variable_name_1",
            "variable_value_1",
            "task_input_variable_name_2",
            "static_value_2"
        );

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            taskVariables
        );

        assertThat(outputVariables).isNotEmpty();
        assertThat(outputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(tuple("process_variable_4", "static_value_2"));
    }

    @Test
    public void should_substituteExpressions_when_expressionIsInProperties() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-properties.json",
            "Process_expressionProperty"
        );

        Map<String, Object> var1 = map(
            "prop1",
            "property 1",
            "prop2",
            "expressionResolved",
            "prop3",
            asList("1", "this expressionResolved is OK", "2")
        );

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);
        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", var1),
                tuple("task_input_variable_name_2", "static_value_1")
            );
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_expressionIsOutputMapping() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue"
        );

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class).isThrownBy(() ->
            variablesMappingProvider.calculateOutPutVariables(
                buildMappingExecutionContext(execution),
                map("task_input_variable_name_1", "variable_value_1", "task_input_variable_name_2", "${expression}")
            )
        );
    }

    @Test
    public void should_throwActivitiIllegalArgumentException_when_expressionIsOutputMappingUsingMapAll()
        throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-mapping-all-output-value.json",
            "Process_expressionMappingOutputValue"
        );

        assertThatExceptionOfType(ActivitiIllegalArgumentException.class).isThrownBy(() ->
            variablesMappingProvider.calculateOutPutVariables(
                buildMappingExecutionContext(execution),
                map("task_input_variable_name_1", "variable_value_1", "task_input_variable_name_2", "${expression}")
            )
        );
    }

    @Test
    public void should_returnResolveToNull_when_resolvingVariablesExpressionInTask() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue"
        );

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            null
        );

        assertThat(outputVariables).containsOnlyKeys("process_variable_4").containsValue(null);
    }

    @Test
    public void should_returnEmptyOutputMapping_when_thereIsAnEmptyValueInOutputMappingVariable() throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "no-value-in-output-mapping-variable.json",
            "Process_noValueOutputMappingVariable"
        );

        Map<String, Object> outputMapping = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            singletonMap("not_matching_variable", "variable_value_1")
        );

        assertThat(outputMapping).isEmpty();
    }

    @Test
    public void should_returnAllExecutionVariables_when_calculatingAnImplicitInputMapping() throws Exception {
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_Two");
        Map<String, Object> executionVariables = map("process_variable_1", "value1", "process_variable_2", "value2");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        given(execution.getVariables()).willReturn(executionVariables);

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isEqualTo(executionVariables);
    }

    @Test
    public void should_returnAllTaskVariables_when_calculatingAnImplicitOutputMapping() throws Exception {
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_One");
        Map<String, Object> taskVariables = map("task_variable_1", "value1", "task_variable_2", "value2");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        ExpressionResolverHelper.setExecutionVariables(execution, taskVariables);

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            taskVariables
        );

        assertThat(outputVariables).isEqualTo(taskVariables);
    }

    @Test
    public void should_calculateInputVariables_when_variableIsInProcessInstanceContextButNotDefinedInExtensions()
        throws Exception {
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_Three");
        given(execution.getVariable("process_variable_inputmap_1")).willReturn("new-input-value");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables.get("task_input_variable_name_1")).isEqualTo("new-input-value");
    }

    @Test
    public void should_calculateOutputVariables_when_variableIsInProcessInstanceContextButNotDefinedInExtensions()
        throws Exception {
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-implicit-mapping-extensions.json"),
            ProcessExtensionModel.class
        );

        Extension processExtensions = extensions.getExtensions("Process_taskImplicitVarMapping");
        DelegateExecution execution = buildExecution(processExtensions, "Task_Three");

        ExpressionResolver expressionResolver = ExpressionResolverHelper.initContext(execution, processExtensions);

        ReflectionTestUtils.setField(variablesMappingProvider, "expressionResolver", expressionResolver);

        Map<String, Object> entityVariables = singletonMap("task_output_variable_name_1", "var-one");

        ExpressionResolverHelper.setExecutionVariables(execution, entityVariables);
        given(execution.getVariable("process_variable_outputmap_1")).willReturn(("process-value"));

        Map<String, Object> outputVariables = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            Map.of("task_output_variable_name_1", "task-value")
        );

        assertThat(outputVariables.get("process_variable_outputmap_1")).isEqualTo("task-value");
    }

    @Test
    public void should_resolveExpressionsBasedInExecutionContext_when_calculatingOutputMappingAndHasExecution()
        throws Exception {
        DelegateExecution execution = initExpressionResolverTest(
            "expression-based-in-execution-in-mapping-output-value.json",
            "Process_expressionMappingOutputValue"
        );

        VariableInstanceEntityImpl variableInstance = new VariableInstanceEntityImpl();
        variableInstance.setTypeName("string");
        variableInstance.setType(new StringType(255));
        variableInstance.setValue("variableValue");
        given(execution.getVariableInstance("process_variable_3")).willReturn(variableInstance);

        Map<String, Object> outputMapping = variablesMappingProvider.calculateOutPutVariables(
            buildMappingExecutionContext(execution),
            null
        );

        assertThat(outputMapping).containsOnlyKeys("process_variable_1", "process_variable_2");
        assertThat(outputMapping.get("process_variable_1")).isNotEqualTo("${authenticatedUserId}");
        assertThat(outputMapping.get("process_variable_2")).isEqualTo("This is the variableValue");
    }

    @Test
    public void should_substituteExpressions_when_customExpression() throws Exception {
        List<CustomFunctionProvider> customFunctionProviders = List.of(new TestCustomFunctionProvider());

        DelegateExecution execution = initExpressionResolverTest(
            "custom-expression-in-mapping-input-value.json",
            "Process_expressionMappingInputValue",
            customFunctionProviders
        );

        Map<String, Object> inputVariables = variablesMappingProvider.calculateInputVariables(execution);

        assertThat(inputVariables).isNotEmpty();
        assertThat(inputVariables.entrySet())
            .extracting(Map.Entry::getKey, Map.Entry::getValue)
            .containsOnly(
                tuple("process_constant_1", "constant_1_value"),
                tuple("process_constant_2", "constant_2_value"),
                tuple("task_input_variable_name_1", 1),
                tuple("task_input_variable_name_2", 2)
            );
    }

    public static class TestCustomFunctionProvider implements CustomFunctionProvider {

        public static Integer plusOne(Integer number) {
            return number + 1;
        }

        @Override
        public void addCustomFunctions(ActivitiElContext elContext) {
            try {
                elContext.setFunction(
                    "",
                    "plusOne",
                    TestCustomFunctionProvider.class.getMethod("plusOne", Integer.class)
                );
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    void should_setMappingEphemeral_basedOn_mappingIsEphemeralOrNot() throws IOException {
        //given
        ProcessExtensionModel extensions = OBJECT_MAPPER.readValue(
            new File("src/test/resources/task-variable-mapping-extensions-with-ephemeral.json"),
            ProcessExtensionModel.class
        );
        Extension processExtensions = extensions.getExtensions("Process_taskVarMapping");
        DelegateExecution executionEphemeralTask = buildExecution(processExtensions, "ephemeralTask");
        DelegateExecution implicitNonEphemeralTask= buildExecution(processExtensions, "implicitNonEphemeralTask");
        DelegateExecution explicitNonEphemeralTask= buildExecution(processExtensions, "explicitNonEphemeralTask");


        //then
        assertThat(variablesMappingProvider.isMappingEphemeral(executionEphemeralTask)).isTrue();
        assertThat(variablesMappingProvider.isMappingEphemeral(implicitNonEphemeralTask)).isFalse();
        assertThat(variablesMappingProvider.isMappingEphemeral(explicitNonEphemeralTask)).isFalse();
    }
}
