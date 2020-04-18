package quasylab.sibilla.lang.pm.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import quasylab.sibilla.lang.pm.services.ModelGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalModelParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_ID", "RULE_INT", "RULE_STRING", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'population'", "'='", "';'", "'const'", "'macro'", "'measure'", "'species'", "'rule'", "'['", "']'", "'-['", "']->'", "'|'", "'&'", "'<'", "'<='", "'=='", "'!='", "'>'", "'>='", "'*'", "'/'", "'//'", "'+'", "'-'", "'%'", "'true'", "'false'", "'!'", "'min'", "'('", "','", "')'", "'max'", "'?'", "':'", "'#'", "'.'", "'E'"
    };
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int RULE_ID=4;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int RULE_INT=5;
    public static final int T__29=29;
    public static final int T__22=22;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int RULE_STRING=6;
    public static final int RULE_SL_COMMENT=8;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int EOF=-1;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int RULE_WS=9;
    public static final int RULE_ANY_OTHER=10;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;

    // delegates
    // delegators


        public InternalModelParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalModelParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalModelParser.tokenNames; }
    public String getGrammarFileName() { return "InternalModel.g"; }



     	private ModelGrammarAccess grammarAccess;

        public InternalModelParser(TokenStream input, ModelGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }

        @Override
        protected String getFirstRuleName() {
        	return "Model";
       	}

       	@Override
       	protected ModelGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}




    // $ANTLR start "entryRuleModel"
    // InternalModel.g:64:1: entryRuleModel returns [EObject current=null] : iv_ruleModel= ruleModel EOF ;
    public final EObject entryRuleModel() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModel = null;


        try {
            // InternalModel.g:64:46: (iv_ruleModel= ruleModel EOF )
            // InternalModel.g:65:2: iv_ruleModel= ruleModel EOF
            {
             newCompositeNode(grammarAccess.getModelRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleModel=ruleModel();

            state._fsp--;

             current =iv_ruleModel; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleModel"


    // $ANTLR start "ruleModel"
    // InternalModel.g:71:1: ruleModel returns [EObject current=null] : ( (lv_elements_0_0= ruleElement ) )* ;
    public final EObject ruleModel() throws RecognitionException {
        EObject current = null;

        EObject lv_elements_0_0 = null;



        	enterRule();

        try {
            // InternalModel.g:77:2: ( ( (lv_elements_0_0= ruleElement ) )* )
            // InternalModel.g:78:2: ( (lv_elements_0_0= ruleElement ) )*
            {
            // InternalModel.g:78:2: ( (lv_elements_0_0= ruleElement ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==11||(LA1_0>=14 && LA1_0<=18)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalModel.g:79:3: (lv_elements_0_0= ruleElement )
            	    {
            	    // InternalModel.g:79:3: (lv_elements_0_0= ruleElement )
            	    // InternalModel.g:80:4: lv_elements_0_0= ruleElement
            	    {

            	    				newCompositeNode(grammarAccess.getModelAccess().getElementsElementParserRuleCall_0());
            	    			
            	    pushFollow(FOLLOW_3);
            	    lv_elements_0_0=ruleElement();

            	    state._fsp--;


            	    				if (current==null) {
            	    					current = createModelElementForParent(grammarAccess.getModelRule());
            	    				}
            	    				add(
            	    					current,
            	    					"elements",
            	    					lv_elements_0_0,
            	    					"quasylab.sibilla.lang.pm.Model.Element");
            	    				afterParserOrEnumRuleCall();
            	    			

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleModel"


    // $ANTLR start "entryRuleElement"
    // InternalModel.g:100:1: entryRuleElement returns [EObject current=null] : iv_ruleElement= ruleElement EOF ;
    public final EObject entryRuleElement() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleElement = null;


        try {
            // InternalModel.g:100:48: (iv_ruleElement= ruleElement EOF )
            // InternalModel.g:101:2: iv_ruleElement= ruleElement EOF
            {
             newCompositeNode(grammarAccess.getElementRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleElement=ruleElement();

            state._fsp--;

             current =iv_ruleElement; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleElement"


    // $ANTLR start "ruleElement"
    // InternalModel.g:107:1: ruleElement returns [EObject current=null] : (this_SpeciesDeclaration_0= ruleSpeciesDeclaration | this_Rule_1= ruleRule | this_Constant_2= ruleConstant | this_Macro_3= ruleMacro | this_System_4= ruleSystem | this_Measure_5= ruleMeasure ) ;
    public final EObject ruleElement() throws RecognitionException {
        EObject current = null;

        EObject this_SpeciesDeclaration_0 = null;

        EObject this_Rule_1 = null;

        EObject this_Constant_2 = null;

        EObject this_Macro_3 = null;

        EObject this_System_4 = null;

        EObject this_Measure_5 = null;



        	enterRule();

        try {
            // InternalModel.g:113:2: ( (this_SpeciesDeclaration_0= ruleSpeciesDeclaration | this_Rule_1= ruleRule | this_Constant_2= ruleConstant | this_Macro_3= ruleMacro | this_System_4= ruleSystem | this_Measure_5= ruleMeasure ) )
            // InternalModel.g:114:2: (this_SpeciesDeclaration_0= ruleSpeciesDeclaration | this_Rule_1= ruleRule | this_Constant_2= ruleConstant | this_Macro_3= ruleMacro | this_System_4= ruleSystem | this_Measure_5= ruleMeasure )
            {
            // InternalModel.g:114:2: (this_SpeciesDeclaration_0= ruleSpeciesDeclaration | this_Rule_1= ruleRule | this_Constant_2= ruleConstant | this_Macro_3= ruleMacro | this_System_4= ruleSystem | this_Measure_5= ruleMeasure )
            int alt2=6;
            switch ( input.LA(1) ) {
            case 17:
                {
                alt2=1;
                }
                break;
            case 18:
                {
                alt2=2;
                }
                break;
            case 14:
                {
                alt2=3;
                }
                break;
            case 15:
                {
                alt2=4;
                }
                break;
            case 11:
                {
                alt2=5;
                }
                break;
            case 16:
                {
                alt2=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // InternalModel.g:115:3: this_SpeciesDeclaration_0= ruleSpeciesDeclaration
                    {

                    			newCompositeNode(grammarAccess.getElementAccess().getSpeciesDeclarationParserRuleCall_0());
                    		
                    pushFollow(FOLLOW_2);
                    this_SpeciesDeclaration_0=ruleSpeciesDeclaration();

                    state._fsp--;


                    			current = this_SpeciesDeclaration_0;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 2 :
                    // InternalModel.g:124:3: this_Rule_1= ruleRule
                    {

                    			newCompositeNode(grammarAccess.getElementAccess().getRuleParserRuleCall_1());
                    		
                    pushFollow(FOLLOW_2);
                    this_Rule_1=ruleRule();

                    state._fsp--;


                    			current = this_Rule_1;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 3 :
                    // InternalModel.g:133:3: this_Constant_2= ruleConstant
                    {

                    			newCompositeNode(grammarAccess.getElementAccess().getConstantParserRuleCall_2());
                    		
                    pushFollow(FOLLOW_2);
                    this_Constant_2=ruleConstant();

                    state._fsp--;


                    			current = this_Constant_2;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 4 :
                    // InternalModel.g:142:3: this_Macro_3= ruleMacro
                    {

                    			newCompositeNode(grammarAccess.getElementAccess().getMacroParserRuleCall_3());
                    		
                    pushFollow(FOLLOW_2);
                    this_Macro_3=ruleMacro();

                    state._fsp--;


                    			current = this_Macro_3;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 5 :
                    // InternalModel.g:151:3: this_System_4= ruleSystem
                    {

                    			newCompositeNode(grammarAccess.getElementAccess().getSystemParserRuleCall_4());
                    		
                    pushFollow(FOLLOW_2);
                    this_System_4=ruleSystem();

                    state._fsp--;


                    			current = this_System_4;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 6 :
                    // InternalModel.g:160:3: this_Measure_5= ruleMeasure
                    {

                    			newCompositeNode(grammarAccess.getElementAccess().getMeasureParserRuleCall_5());
                    		
                    pushFollow(FOLLOW_2);
                    this_Measure_5=ruleMeasure();

                    state._fsp--;


                    			current = this_Measure_5;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleElement"


    // $ANTLR start "entryRuleSystem"
    // InternalModel.g:172:1: entryRuleSystem returns [EObject current=null] : iv_ruleSystem= ruleSystem EOF ;
    public final EObject entryRuleSystem() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSystem = null;


        try {
            // InternalModel.g:172:47: (iv_ruleSystem= ruleSystem EOF )
            // InternalModel.g:173:2: iv_ruleSystem= ruleSystem EOF
            {
             newCompositeNode(grammarAccess.getSystemRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSystem=ruleSystem();

            state._fsp--;

             current =iv_ruleSystem; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSystem"


    // $ANTLR start "ruleSystem"
    // InternalModel.g:179:1: ruleSystem returns [EObject current=null] : (otherlv_0= 'population' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_population_3_0= rulePopulation ) ) otherlv_4= ';' ) ;
    public final EObject ruleSystem() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_population_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:185:2: ( (otherlv_0= 'population' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_population_3_0= rulePopulation ) ) otherlv_4= ';' ) )
            // InternalModel.g:186:2: (otherlv_0= 'population' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_population_3_0= rulePopulation ) ) otherlv_4= ';' )
            {
            // InternalModel.g:186:2: (otherlv_0= 'population' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_population_3_0= rulePopulation ) ) otherlv_4= ';' )
            // InternalModel.g:187:3: otherlv_0= 'population' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_population_3_0= rulePopulation ) ) otherlv_4= ';'
            {
            otherlv_0=(Token)match(input,11,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getSystemAccess().getPopulationKeyword_0());
            		
            // InternalModel.g:191:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalModel.g:192:4: (lv_name_1_0= RULE_ID )
            {
            // InternalModel.g:192:4: (lv_name_1_0= RULE_ID )
            // InternalModel.g:193:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_5); 

            					newLeafNode(lv_name_1_0, grammarAccess.getSystemAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSystemRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            otherlv_2=(Token)match(input,12,FOLLOW_6); 

            			newLeafNode(otherlv_2, grammarAccess.getSystemAccess().getEqualsSignKeyword_2());
            		
            // InternalModel.g:213:3: ( (lv_population_3_0= rulePopulation ) )
            // InternalModel.g:214:4: (lv_population_3_0= rulePopulation )
            {
            // InternalModel.g:214:4: (lv_population_3_0= rulePopulation )
            // InternalModel.g:215:5: lv_population_3_0= rulePopulation
            {

            					newCompositeNode(grammarAccess.getSystemAccess().getPopulationPopulationParserRuleCall_3_0());
            				
            pushFollow(FOLLOW_7);
            lv_population_3_0=rulePopulation();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getSystemRule());
            					}
            					set(
            						current,
            						"population",
            						lv_population_3_0,
            						"quasylab.sibilla.lang.pm.Model.Population");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_4=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_4, grammarAccess.getSystemAccess().getSemicolonKeyword_4());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSystem"


    // $ANTLR start "entryRuleConstant"
    // InternalModel.g:240:1: entryRuleConstant returns [EObject current=null] : iv_ruleConstant= ruleConstant EOF ;
    public final EObject entryRuleConstant() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleConstant = null;


        try {
            // InternalModel.g:240:49: (iv_ruleConstant= ruleConstant EOF )
            // InternalModel.g:241:2: iv_ruleConstant= ruleConstant EOF
            {
             newCompositeNode(grammarAccess.getConstantRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleConstant=ruleConstant();

            state._fsp--;

             current =iv_ruleConstant; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleConstant"


    // $ANTLR start "ruleConstant"
    // InternalModel.g:247:1: ruleConstant returns [EObject current=null] : (otherlv_0= 'const' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' ) ;
    public final EObject ruleConstant() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_value_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:253:2: ( (otherlv_0= 'const' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' ) )
            // InternalModel.g:254:2: (otherlv_0= 'const' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' )
            {
            // InternalModel.g:254:2: (otherlv_0= 'const' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' )
            // InternalModel.g:255:3: otherlv_0= 'const' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';'
            {
            otherlv_0=(Token)match(input,14,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getConstantAccess().getConstKeyword_0());
            		
            // InternalModel.g:259:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalModel.g:260:4: (lv_name_1_0= RULE_ID )
            {
            // InternalModel.g:260:4: (lv_name_1_0= RULE_ID )
            // InternalModel.g:261:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_5); 

            					newLeafNode(lv_name_1_0, grammarAccess.getConstantAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getConstantRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            otherlv_2=(Token)match(input,12,FOLLOW_8); 

            			newLeafNode(otherlv_2, grammarAccess.getConstantAccess().getEqualsSignKeyword_2());
            		
            // InternalModel.g:281:3: ( (lv_value_3_0= ruleExpression ) )
            // InternalModel.g:282:4: (lv_value_3_0= ruleExpression )
            {
            // InternalModel.g:282:4: (lv_value_3_0= ruleExpression )
            // InternalModel.g:283:5: lv_value_3_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getConstantAccess().getValueExpressionParserRuleCall_3_0());
            				
            pushFollow(FOLLOW_7);
            lv_value_3_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getConstantRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_3_0,
            						"quasylab.sibilla.lang.pm.Model.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_4=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_4, grammarAccess.getConstantAccess().getSemicolonKeyword_4());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleConstant"


    // $ANTLR start "entryRuleMacro"
    // InternalModel.g:308:1: entryRuleMacro returns [EObject current=null] : iv_ruleMacro= ruleMacro EOF ;
    public final EObject entryRuleMacro() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMacro = null;


        try {
            // InternalModel.g:308:46: (iv_ruleMacro= ruleMacro EOF )
            // InternalModel.g:309:2: iv_ruleMacro= ruleMacro EOF
            {
             newCompositeNode(grammarAccess.getMacroRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleMacro=ruleMacro();

            state._fsp--;

             current =iv_ruleMacro; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMacro"


    // $ANTLR start "ruleMacro"
    // InternalModel.g:315:1: ruleMacro returns [EObject current=null] : (otherlv_0= 'macro' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' ) ;
    public final EObject ruleMacro() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_value_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:321:2: ( (otherlv_0= 'macro' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' ) )
            // InternalModel.g:322:2: (otherlv_0= 'macro' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' )
            {
            // InternalModel.g:322:2: (otherlv_0= 'macro' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' )
            // InternalModel.g:323:3: otherlv_0= 'macro' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';'
            {
            otherlv_0=(Token)match(input,15,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getMacroAccess().getMacroKeyword_0());
            		
            // InternalModel.g:327:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalModel.g:328:4: (lv_name_1_0= RULE_ID )
            {
            // InternalModel.g:328:4: (lv_name_1_0= RULE_ID )
            // InternalModel.g:329:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_5); 

            					newLeafNode(lv_name_1_0, grammarAccess.getMacroAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMacroRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            otherlv_2=(Token)match(input,12,FOLLOW_8); 

            			newLeafNode(otherlv_2, grammarAccess.getMacroAccess().getEqualsSignKeyword_2());
            		
            // InternalModel.g:349:3: ( (lv_value_3_0= ruleExpression ) )
            // InternalModel.g:350:4: (lv_value_3_0= ruleExpression )
            {
            // InternalModel.g:350:4: (lv_value_3_0= ruleExpression )
            // InternalModel.g:351:5: lv_value_3_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getMacroAccess().getValueExpressionParserRuleCall_3_0());
            				
            pushFollow(FOLLOW_7);
            lv_value_3_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getMacroRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_3_0,
            						"quasylab.sibilla.lang.pm.Model.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_4=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_4, grammarAccess.getMacroAccess().getSemicolonKeyword_4());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMacro"


    // $ANTLR start "entryRuleMeasure"
    // InternalModel.g:376:1: entryRuleMeasure returns [EObject current=null] : iv_ruleMeasure= ruleMeasure EOF ;
    public final EObject entryRuleMeasure() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMeasure = null;


        try {
            // InternalModel.g:376:48: (iv_ruleMeasure= ruleMeasure EOF )
            // InternalModel.g:377:2: iv_ruleMeasure= ruleMeasure EOF
            {
             newCompositeNode(grammarAccess.getMeasureRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleMeasure=ruleMeasure();

            state._fsp--;

             current =iv_ruleMeasure; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMeasure"


    // $ANTLR start "ruleMeasure"
    // InternalModel.g:383:1: ruleMeasure returns [EObject current=null] : (otherlv_0= 'measure' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' ) ;
    public final EObject ruleMeasure() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        EObject lv_value_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:389:2: ( (otherlv_0= 'measure' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' ) )
            // InternalModel.g:390:2: (otherlv_0= 'measure' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' )
            {
            // InternalModel.g:390:2: (otherlv_0= 'measure' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';' )
            // InternalModel.g:391:3: otherlv_0= 'measure' ( (lv_name_1_0= RULE_ID ) ) otherlv_2= '=' ( (lv_value_3_0= ruleExpression ) ) otherlv_4= ';'
            {
            otherlv_0=(Token)match(input,16,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getMeasureAccess().getMeasureKeyword_0());
            		
            // InternalModel.g:395:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalModel.g:396:4: (lv_name_1_0= RULE_ID )
            {
            // InternalModel.g:396:4: (lv_name_1_0= RULE_ID )
            // InternalModel.g:397:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_5); 

            					newLeafNode(lv_name_1_0, grammarAccess.getMeasureAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMeasureRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            otherlv_2=(Token)match(input,12,FOLLOW_8); 

            			newLeafNode(otherlv_2, grammarAccess.getMeasureAccess().getEqualsSignKeyword_2());
            		
            // InternalModel.g:417:3: ( (lv_value_3_0= ruleExpression ) )
            // InternalModel.g:418:4: (lv_value_3_0= ruleExpression )
            {
            // InternalModel.g:418:4: (lv_value_3_0= ruleExpression )
            // InternalModel.g:419:5: lv_value_3_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getMeasureAccess().getValueExpressionParserRuleCall_3_0());
            				
            pushFollow(FOLLOW_7);
            lv_value_3_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getMeasureRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_3_0,
            						"quasylab.sibilla.lang.pm.Model.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_4=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_4, grammarAccess.getMeasureAccess().getSemicolonKeyword_4());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMeasure"


    // $ANTLR start "entryRuleSpeciesDeclaration"
    // InternalModel.g:444:1: entryRuleSpeciesDeclaration returns [EObject current=null] : iv_ruleSpeciesDeclaration= ruleSpeciesDeclaration EOF ;
    public final EObject entryRuleSpeciesDeclaration() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSpeciesDeclaration = null;


        try {
            // InternalModel.g:444:59: (iv_ruleSpeciesDeclaration= ruleSpeciesDeclaration EOF )
            // InternalModel.g:445:2: iv_ruleSpeciesDeclaration= ruleSpeciesDeclaration EOF
            {
             newCompositeNode(grammarAccess.getSpeciesDeclarationRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSpeciesDeclaration=ruleSpeciesDeclaration();

            state._fsp--;

             current =iv_ruleSpeciesDeclaration; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSpeciesDeclaration"


    // $ANTLR start "ruleSpeciesDeclaration"
    // InternalModel.g:451:1: ruleSpeciesDeclaration returns [EObject current=null] : (otherlv_0= 'species' this_Species_1= ruleSpecies otherlv_2= ';' ) ;
    public final EObject ruleSpeciesDeclaration() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_2=null;
        EObject this_Species_1 = null;



        	enterRule();

        try {
            // InternalModel.g:457:2: ( (otherlv_0= 'species' this_Species_1= ruleSpecies otherlv_2= ';' ) )
            // InternalModel.g:458:2: (otherlv_0= 'species' this_Species_1= ruleSpecies otherlv_2= ';' )
            {
            // InternalModel.g:458:2: (otherlv_0= 'species' this_Species_1= ruleSpecies otherlv_2= ';' )
            // InternalModel.g:459:3: otherlv_0= 'species' this_Species_1= ruleSpecies otherlv_2= ';'
            {
            otherlv_0=(Token)match(input,17,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getSpeciesDeclarationAccess().getSpeciesKeyword_0());
            		

            			newCompositeNode(grammarAccess.getSpeciesDeclarationAccess().getSpeciesParserRuleCall_1());
            		
            pushFollow(FOLLOW_7);
            this_Species_1=ruleSpecies();

            state._fsp--;


            			current = this_Species_1;
            			afterParserOrEnumRuleCall();
            		
            otherlv_2=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_2, grammarAccess.getSpeciesDeclarationAccess().getSemicolonKeyword_2());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSpeciesDeclaration"


    // $ANTLR start "entryRuleSpecies"
    // InternalModel.g:479:1: entryRuleSpecies returns [EObject current=null] : iv_ruleSpecies= ruleSpecies EOF ;
    public final EObject entryRuleSpecies() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSpecies = null;


        try {
            // InternalModel.g:479:48: (iv_ruleSpecies= ruleSpecies EOF )
            // InternalModel.g:480:2: iv_ruleSpecies= ruleSpecies EOF
            {
             newCompositeNode(grammarAccess.getSpeciesRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSpecies=ruleSpecies();

            state._fsp--;

             current =iv_ruleSpecies; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSpecies"


    // $ANTLR start "ruleSpecies"
    // InternalModel.g:486:1: ruleSpecies returns [EObject current=null] : ( (lv_name_0_0= RULE_ID ) ) ;
    public final EObject ruleSpecies() throws RecognitionException {
        EObject current = null;

        Token lv_name_0_0=null;


        	enterRule();

        try {
            // InternalModel.g:492:2: ( ( (lv_name_0_0= RULE_ID ) ) )
            // InternalModel.g:493:2: ( (lv_name_0_0= RULE_ID ) )
            {
            // InternalModel.g:493:2: ( (lv_name_0_0= RULE_ID ) )
            // InternalModel.g:494:3: (lv_name_0_0= RULE_ID )
            {
            // InternalModel.g:494:3: (lv_name_0_0= RULE_ID )
            // InternalModel.g:495:4: lv_name_0_0= RULE_ID
            {
            lv_name_0_0=(Token)match(input,RULE_ID,FOLLOW_2); 

            				newLeafNode(lv_name_0_0, grammarAccess.getSpeciesAccess().getNameIDTerminalRuleCall_0());
            			

            				if (current==null) {
            					current = createModelElement(grammarAccess.getSpeciesRule());
            				}
            				setWithLastConsumed(
            					current,
            					"name",
            					lv_name_0_0,
            					"org.eclipse.xtext.common.Terminals.ID");
            			

            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSpecies"


    // $ANTLR start "entryRuleRule"
    // InternalModel.g:514:1: entryRuleRule returns [EObject current=null] : iv_ruleRule= ruleRule EOF ;
    public final EObject entryRuleRule() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRule = null;


        try {
            // InternalModel.g:514:45: (iv_ruleRule= ruleRule EOF )
            // InternalModel.g:515:2: iv_ruleRule= ruleRule EOF
            {
             newCompositeNode(grammarAccess.getRuleRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRule=ruleRule();

            state._fsp--;

             current =iv_ruleRule; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRule"


    // $ANTLR start "ruleRule"
    // InternalModel.g:521:1: ruleRule returns [EObject current=null] : (otherlv_0= 'rule' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_guard_3_0= ruleExpression ) ) otherlv_4= ']' )? otherlv_5= '=' ( (lv_pre_6_0= rulePopulation ) ) otherlv_7= '-[' ( (lv_rate_8_0= ruleExpression ) ) otherlv_9= ']->' ( (lv_post_10_0= rulePopulation ) ) otherlv_11= ';' ) ;
    public final EObject ruleRule() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token otherlv_11=null;
        EObject lv_guard_3_0 = null;

        EObject lv_pre_6_0 = null;

        EObject lv_rate_8_0 = null;

        EObject lv_post_10_0 = null;



        	enterRule();

        try {
            // InternalModel.g:527:2: ( (otherlv_0= 'rule' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_guard_3_0= ruleExpression ) ) otherlv_4= ']' )? otherlv_5= '=' ( (lv_pre_6_0= rulePopulation ) ) otherlv_7= '-[' ( (lv_rate_8_0= ruleExpression ) ) otherlv_9= ']->' ( (lv_post_10_0= rulePopulation ) ) otherlv_11= ';' ) )
            // InternalModel.g:528:2: (otherlv_0= 'rule' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_guard_3_0= ruleExpression ) ) otherlv_4= ']' )? otherlv_5= '=' ( (lv_pre_6_0= rulePopulation ) ) otherlv_7= '-[' ( (lv_rate_8_0= ruleExpression ) ) otherlv_9= ']->' ( (lv_post_10_0= rulePopulation ) ) otherlv_11= ';' )
            {
            // InternalModel.g:528:2: (otherlv_0= 'rule' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_guard_3_0= ruleExpression ) ) otherlv_4= ']' )? otherlv_5= '=' ( (lv_pre_6_0= rulePopulation ) ) otherlv_7= '-[' ( (lv_rate_8_0= ruleExpression ) ) otherlv_9= ']->' ( (lv_post_10_0= rulePopulation ) ) otherlv_11= ';' )
            // InternalModel.g:529:3: otherlv_0= 'rule' ( (lv_name_1_0= RULE_ID ) ) (otherlv_2= '[' ( (lv_guard_3_0= ruleExpression ) ) otherlv_4= ']' )? otherlv_5= '=' ( (lv_pre_6_0= rulePopulation ) ) otherlv_7= '-[' ( (lv_rate_8_0= ruleExpression ) ) otherlv_9= ']->' ( (lv_post_10_0= rulePopulation ) ) otherlv_11= ';'
            {
            otherlv_0=(Token)match(input,18,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getRuleAccess().getRuleKeyword_0());
            		
            // InternalModel.g:533:3: ( (lv_name_1_0= RULE_ID ) )
            // InternalModel.g:534:4: (lv_name_1_0= RULE_ID )
            {
            // InternalModel.g:534:4: (lv_name_1_0= RULE_ID )
            // InternalModel.g:535:5: lv_name_1_0= RULE_ID
            {
            lv_name_1_0=(Token)match(input,RULE_ID,FOLLOW_9); 

            					newLeafNode(lv_name_1_0, grammarAccess.getRuleAccess().getNameIDTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRuleRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.ID");
            				

            }


            }

            // InternalModel.g:551:3: (otherlv_2= '[' ( (lv_guard_3_0= ruleExpression ) ) otherlv_4= ']' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==19) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // InternalModel.g:552:4: otherlv_2= '[' ( (lv_guard_3_0= ruleExpression ) ) otherlv_4= ']'
                    {
                    otherlv_2=(Token)match(input,19,FOLLOW_8); 

                    				newLeafNode(otherlv_2, grammarAccess.getRuleAccess().getLeftSquareBracketKeyword_2_0());
                    			
                    // InternalModel.g:556:4: ( (lv_guard_3_0= ruleExpression ) )
                    // InternalModel.g:557:5: (lv_guard_3_0= ruleExpression )
                    {
                    // InternalModel.g:557:5: (lv_guard_3_0= ruleExpression )
                    // InternalModel.g:558:6: lv_guard_3_0= ruleExpression
                    {

                    						newCompositeNode(grammarAccess.getRuleAccess().getGuardExpressionParserRuleCall_2_1_0());
                    					
                    pushFollow(FOLLOW_10);
                    lv_guard_3_0=ruleExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getRuleRule());
                    						}
                    						set(
                    							current,
                    							"guard",
                    							lv_guard_3_0,
                    							"quasylab.sibilla.lang.pm.Model.Expression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }

                    otherlv_4=(Token)match(input,20,FOLLOW_5); 

                    				newLeafNode(otherlv_4, grammarAccess.getRuleAccess().getRightSquareBracketKeyword_2_2());
                    			

                    }
                    break;

            }

            otherlv_5=(Token)match(input,12,FOLLOW_11); 

            			newLeafNode(otherlv_5, grammarAccess.getRuleAccess().getEqualsSignKeyword_3());
            		
            // InternalModel.g:584:3: ( (lv_pre_6_0= rulePopulation ) )
            // InternalModel.g:585:4: (lv_pre_6_0= rulePopulation )
            {
            // InternalModel.g:585:4: (lv_pre_6_0= rulePopulation )
            // InternalModel.g:586:5: lv_pre_6_0= rulePopulation
            {

            					newCompositeNode(grammarAccess.getRuleAccess().getPrePopulationParserRuleCall_4_0());
            				
            pushFollow(FOLLOW_12);
            lv_pre_6_0=rulePopulation();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRuleRule());
            					}
            					set(
            						current,
            						"pre",
            						lv_pre_6_0,
            						"quasylab.sibilla.lang.pm.Model.Population");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_7=(Token)match(input,21,FOLLOW_8); 

            			newLeafNode(otherlv_7, grammarAccess.getRuleAccess().getHyphenMinusLeftSquareBracketKeyword_5());
            		
            // InternalModel.g:607:3: ( (lv_rate_8_0= ruleExpression ) )
            // InternalModel.g:608:4: (lv_rate_8_0= ruleExpression )
            {
            // InternalModel.g:608:4: (lv_rate_8_0= ruleExpression )
            // InternalModel.g:609:5: lv_rate_8_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getRuleAccess().getRateExpressionParserRuleCall_6_0());
            				
            pushFollow(FOLLOW_13);
            lv_rate_8_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRuleRule());
            					}
            					set(
            						current,
            						"rate",
            						lv_rate_8_0,
            						"quasylab.sibilla.lang.pm.Model.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_9=(Token)match(input,22,FOLLOW_6); 

            			newLeafNode(otherlv_9, grammarAccess.getRuleAccess().getRightSquareBracketHyphenMinusGreaterThanSignKeyword_7());
            		
            // InternalModel.g:630:3: ( (lv_post_10_0= rulePopulation ) )
            // InternalModel.g:631:4: (lv_post_10_0= rulePopulation )
            {
            // InternalModel.g:631:4: (lv_post_10_0= rulePopulation )
            // InternalModel.g:632:5: lv_post_10_0= rulePopulation
            {

            					newCompositeNode(grammarAccess.getRuleAccess().getPostPopulationParserRuleCall_8_0());
            				
            pushFollow(FOLLOW_7);
            lv_post_10_0=rulePopulation();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRuleRule());
            					}
            					set(
            						current,
            						"post",
            						lv_post_10_0,
            						"quasylab.sibilla.lang.pm.Model.Population");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_11=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_11, grammarAccess.getRuleAccess().getSemicolonKeyword_9());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRule"


    // $ANTLR start "entryRulePopulation"
    // InternalModel.g:657:1: entryRulePopulation returns [EObject current=null] : iv_rulePopulation= rulePopulation EOF ;
    public final EObject entryRulePopulation() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePopulation = null;


        try {
            // InternalModel.g:657:51: (iv_rulePopulation= rulePopulation EOF )
            // InternalModel.g:658:2: iv_rulePopulation= rulePopulation EOF
            {
             newCompositeNode(grammarAccess.getPopulationRule()); 
            pushFollow(FOLLOW_1);
            iv_rulePopulation=rulePopulation();

            state._fsp--;

             current =iv_rulePopulation; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePopulation"


    // $ANTLR start "rulePopulation"
    // InternalModel.g:664:1: rulePopulation returns [EObject current=null] : ( () ( ( (lv_population_1_0= ruleMultiplicity ) ) (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )* )? ) ;
    public final EObject rulePopulation() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject lv_population_1_0 = null;

        EObject lv_population_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:670:2: ( ( () ( ( (lv_population_1_0= ruleMultiplicity ) ) (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )* )? ) )
            // InternalModel.g:671:2: ( () ( ( (lv_population_1_0= ruleMultiplicity ) ) (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )* )? )
            {
            // InternalModel.g:671:2: ( () ( ( (lv_population_1_0= ruleMultiplicity ) ) (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )* )? )
            // InternalModel.g:672:3: () ( ( (lv_population_1_0= ruleMultiplicity ) ) (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )* )?
            {
            // InternalModel.g:672:3: ()
            // InternalModel.g:673:4: 
            {

            				current = forceCreateModelElement(
            					grammarAccess.getPopulationAccess().getPopulationAction_0(),
            					current);
            			

            }

            // InternalModel.g:679:3: ( ( (lv_population_1_0= ruleMultiplicity ) ) (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )* )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==RULE_ID) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // InternalModel.g:680:4: ( (lv_population_1_0= ruleMultiplicity ) ) (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )*
                    {
                    // InternalModel.g:680:4: ( (lv_population_1_0= ruleMultiplicity ) )
                    // InternalModel.g:681:5: (lv_population_1_0= ruleMultiplicity )
                    {
                    // InternalModel.g:681:5: (lv_population_1_0= ruleMultiplicity )
                    // InternalModel.g:682:6: lv_population_1_0= ruleMultiplicity
                    {

                    						newCompositeNode(grammarAccess.getPopulationAccess().getPopulationMultiplicityParserRuleCall_1_0_0());
                    					
                    pushFollow(FOLLOW_14);
                    lv_population_1_0=ruleMultiplicity();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getPopulationRule());
                    						}
                    						add(
                    							current,
                    							"population",
                    							lv_population_1_0,
                    							"quasylab.sibilla.lang.pm.Model.Multiplicity");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }

                    // InternalModel.g:699:4: (otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) ) )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==23) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // InternalModel.g:700:5: otherlv_2= '|' ( (lv_population_3_0= ruleMultiplicity ) )
                    	    {
                    	    otherlv_2=(Token)match(input,23,FOLLOW_4); 

                    	    					newLeafNode(otherlv_2, grammarAccess.getPopulationAccess().getVerticalLineKeyword_1_1_0());
                    	    				
                    	    // InternalModel.g:704:5: ( (lv_population_3_0= ruleMultiplicity ) )
                    	    // InternalModel.g:705:6: (lv_population_3_0= ruleMultiplicity )
                    	    {
                    	    // InternalModel.g:705:6: (lv_population_3_0= ruleMultiplicity )
                    	    // InternalModel.g:706:7: lv_population_3_0= ruleMultiplicity
                    	    {

                    	    							newCompositeNode(grammarAccess.getPopulationAccess().getPopulationMultiplicityParserRuleCall_1_1_1_0());
                    	    						
                    	    pushFollow(FOLLOW_14);
                    	    lv_population_3_0=ruleMultiplicity();

                    	    state._fsp--;


                    	    							if (current==null) {
                    	    								current = createModelElementForParent(grammarAccess.getPopulationRule());
                    	    							}
                    	    							add(
                    	    								current,
                    	    								"population",
                    	    								lv_population_3_0,
                    	    								"quasylab.sibilla.lang.pm.Model.Multiplicity");
                    	    							afterParserOrEnumRuleCall();
                    	    						

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    }
                    break;

            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePopulation"


    // $ANTLR start "entryRuleMultiplicity"
    // InternalModel.g:729:1: entryRuleMultiplicity returns [EObject current=null] : iv_ruleMultiplicity= ruleMultiplicity EOF ;
    public final EObject entryRuleMultiplicity() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMultiplicity = null;


        try {
            // InternalModel.g:729:53: (iv_ruleMultiplicity= ruleMultiplicity EOF )
            // InternalModel.g:730:2: iv_ruleMultiplicity= ruleMultiplicity EOF
            {
             newCompositeNode(grammarAccess.getMultiplicityRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleMultiplicity=ruleMultiplicity();

            state._fsp--;

             current =iv_ruleMultiplicity; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMultiplicity"


    // $ANTLR start "ruleMultiplicity"
    // InternalModel.g:736:1: ruleMultiplicity returns [EObject current=null] : ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '[' ( (lv_size_2_0= ruleExpression ) ) otherlv_3= ']' )? ) ;
    public final EObject ruleMultiplicity() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_size_2_0 = null;



        	enterRule();

        try {
            // InternalModel.g:742:2: ( ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '[' ( (lv_size_2_0= ruleExpression ) ) otherlv_3= ']' )? ) )
            // InternalModel.g:743:2: ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '[' ( (lv_size_2_0= ruleExpression ) ) otherlv_3= ']' )? )
            {
            // InternalModel.g:743:2: ( ( (otherlv_0= RULE_ID ) ) (otherlv_1= '[' ( (lv_size_2_0= ruleExpression ) ) otherlv_3= ']' )? )
            // InternalModel.g:744:3: ( (otherlv_0= RULE_ID ) ) (otherlv_1= '[' ( (lv_size_2_0= ruleExpression ) ) otherlv_3= ']' )?
            {
            // InternalModel.g:744:3: ( (otherlv_0= RULE_ID ) )
            // InternalModel.g:745:4: (otherlv_0= RULE_ID )
            {
            // InternalModel.g:745:4: (otherlv_0= RULE_ID )
            // InternalModel.g:746:5: otherlv_0= RULE_ID
            {

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMultiplicityRule());
            					}
            				
            otherlv_0=(Token)match(input,RULE_ID,FOLLOW_15); 

            					newLeafNode(otherlv_0, grammarAccess.getMultiplicityAccess().getSpeciesSpeciesCrossReference_0_0());
            				

            }


            }

            // InternalModel.g:757:3: (otherlv_1= '[' ( (lv_size_2_0= ruleExpression ) ) otherlv_3= ']' )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==19) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // InternalModel.g:758:4: otherlv_1= '[' ( (lv_size_2_0= ruleExpression ) ) otherlv_3= ']'
                    {
                    otherlv_1=(Token)match(input,19,FOLLOW_8); 

                    				newLeafNode(otherlv_1, grammarAccess.getMultiplicityAccess().getLeftSquareBracketKeyword_1_0());
                    			
                    // InternalModel.g:762:4: ( (lv_size_2_0= ruleExpression ) )
                    // InternalModel.g:763:5: (lv_size_2_0= ruleExpression )
                    {
                    // InternalModel.g:763:5: (lv_size_2_0= ruleExpression )
                    // InternalModel.g:764:6: lv_size_2_0= ruleExpression
                    {

                    						newCompositeNode(grammarAccess.getMultiplicityAccess().getSizeExpressionParserRuleCall_1_1_0());
                    					
                    pushFollow(FOLLOW_10);
                    lv_size_2_0=ruleExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getMultiplicityRule());
                    						}
                    						set(
                    							current,
                    							"size",
                    							lv_size_2_0,
                    							"quasylab.sibilla.lang.pm.Model.Expression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }

                    otherlv_3=(Token)match(input,20,FOLLOW_2); 

                    				newLeafNode(otherlv_3, grammarAccess.getMultiplicityAccess().getRightSquareBracketKeyword_1_2());
                    			

                    }
                    break;

            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMultiplicity"


    // $ANTLR start "entryRuleExpression"
    // InternalModel.g:790:1: entryRuleExpression returns [EObject current=null] : iv_ruleExpression= ruleExpression EOF ;
    public final EObject entryRuleExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpression = null;


        try {
            // InternalModel.g:790:51: (iv_ruleExpression= ruleExpression EOF )
            // InternalModel.g:791:2: iv_ruleExpression= ruleExpression EOF
            {
             newCompositeNode(grammarAccess.getExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleExpression=ruleExpression();

            state._fsp--;

             current =iv_ruleExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExpression"


    // $ANTLR start "ruleExpression"
    // InternalModel.g:797:1: ruleExpression returns [EObject current=null] : this_OrExpression_0= ruleOrExpression ;
    public final EObject ruleExpression() throws RecognitionException {
        EObject current = null;

        EObject this_OrExpression_0 = null;



        	enterRule();

        try {
            // InternalModel.g:803:2: (this_OrExpression_0= ruleOrExpression )
            // InternalModel.g:804:2: this_OrExpression_0= ruleOrExpression
            {

            		newCompositeNode(grammarAccess.getExpressionAccess().getOrExpressionParserRuleCall());
            	
            pushFollow(FOLLOW_2);
            this_OrExpression_0=ruleOrExpression();

            state._fsp--;


            		current = this_OrExpression_0;
            		afterParserOrEnumRuleCall();
            	

            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExpression"


    // $ANTLR start "entryRuleOrExpression"
    // InternalModel.g:815:1: entryRuleOrExpression returns [EObject current=null] : iv_ruleOrExpression= ruleOrExpression EOF ;
    public final EObject entryRuleOrExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleOrExpression = null;


        try {
            // InternalModel.g:815:53: (iv_ruleOrExpression= ruleOrExpression EOF )
            // InternalModel.g:816:2: iv_ruleOrExpression= ruleOrExpression EOF
            {
             newCompositeNode(grammarAccess.getOrExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleOrExpression=ruleOrExpression();

            state._fsp--;

             current =iv_ruleOrExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOrExpression"


    // $ANTLR start "ruleOrExpression"
    // InternalModel.g:822:1: ruleOrExpression returns [EObject current=null] : (this_AndExpression_0= ruleAndExpression ( () otherlv_2= '|' ( (lv_right_3_0= ruleAndExpression ) ) )* ) ;
    public final EObject ruleOrExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject this_AndExpression_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:828:2: ( (this_AndExpression_0= ruleAndExpression ( () otherlv_2= '|' ( (lv_right_3_0= ruleAndExpression ) ) )* ) )
            // InternalModel.g:829:2: (this_AndExpression_0= ruleAndExpression ( () otherlv_2= '|' ( (lv_right_3_0= ruleAndExpression ) ) )* )
            {
            // InternalModel.g:829:2: (this_AndExpression_0= ruleAndExpression ( () otherlv_2= '|' ( (lv_right_3_0= ruleAndExpression ) ) )* )
            // InternalModel.g:830:3: this_AndExpression_0= ruleAndExpression ( () otherlv_2= '|' ( (lv_right_3_0= ruleAndExpression ) ) )*
            {

            			newCompositeNode(grammarAccess.getOrExpressionAccess().getAndExpressionParserRuleCall_0());
            		
            pushFollow(FOLLOW_14);
            this_AndExpression_0=ruleAndExpression();

            state._fsp--;


            			current = this_AndExpression_0;
            			afterParserOrEnumRuleCall();
            		
            // InternalModel.g:838:3: ( () otherlv_2= '|' ( (lv_right_3_0= ruleAndExpression ) ) )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==23) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // InternalModel.g:839:4: () otherlv_2= '|' ( (lv_right_3_0= ruleAndExpression ) )
            	    {
            	    // InternalModel.g:839:4: ()
            	    // InternalModel.g:840:5: 
            	    {

            	    					current = forceCreateModelElementAndSet(
            	    						grammarAccess.getOrExpressionAccess().getOrExpressionLeftAction_1_0(),
            	    						current);
            	    				

            	    }

            	    otherlv_2=(Token)match(input,23,FOLLOW_8); 

            	    				newLeafNode(otherlv_2, grammarAccess.getOrExpressionAccess().getVerticalLineKeyword_1_1());
            	    			
            	    // InternalModel.g:850:4: ( (lv_right_3_0= ruleAndExpression ) )
            	    // InternalModel.g:851:5: (lv_right_3_0= ruleAndExpression )
            	    {
            	    // InternalModel.g:851:5: (lv_right_3_0= ruleAndExpression )
            	    // InternalModel.g:852:6: lv_right_3_0= ruleAndExpression
            	    {

            	    						newCompositeNode(grammarAccess.getOrExpressionAccess().getRightAndExpressionParserRuleCall_1_2_0());
            	    					
            	    pushFollow(FOLLOW_14);
            	    lv_right_3_0=ruleAndExpression();

            	    state._fsp--;


            	    						if (current==null) {
            	    							current = createModelElementForParent(grammarAccess.getOrExpressionRule());
            	    						}
            	    						set(
            	    							current,
            	    							"right",
            	    							lv_right_3_0,
            	    							"quasylab.sibilla.lang.pm.Model.AndExpression");
            	    						afterParserOrEnumRuleCall();
            	    					

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOrExpression"


    // $ANTLR start "entryRuleAndExpression"
    // InternalModel.g:874:1: entryRuleAndExpression returns [EObject current=null] : iv_ruleAndExpression= ruleAndExpression EOF ;
    public final EObject entryRuleAndExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAndExpression = null;


        try {
            // InternalModel.g:874:54: (iv_ruleAndExpression= ruleAndExpression EOF )
            // InternalModel.g:875:2: iv_ruleAndExpression= ruleAndExpression EOF
            {
             newCompositeNode(grammarAccess.getAndExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAndExpression=ruleAndExpression();

            state._fsp--;

             current =iv_ruleAndExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAndExpression"


    // $ANTLR start "ruleAndExpression"
    // InternalModel.g:881:1: ruleAndExpression returns [EObject current=null] : (this_RelationExpression_0= ruleRelationExpression ( () otherlv_2= '&' ( (lv_right_3_0= ruleRelationExpression ) ) )* ) ;
    public final EObject ruleAndExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject this_RelationExpression_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:887:2: ( (this_RelationExpression_0= ruleRelationExpression ( () otherlv_2= '&' ( (lv_right_3_0= ruleRelationExpression ) ) )* ) )
            // InternalModel.g:888:2: (this_RelationExpression_0= ruleRelationExpression ( () otherlv_2= '&' ( (lv_right_3_0= ruleRelationExpression ) ) )* )
            {
            // InternalModel.g:888:2: (this_RelationExpression_0= ruleRelationExpression ( () otherlv_2= '&' ( (lv_right_3_0= ruleRelationExpression ) ) )* )
            // InternalModel.g:889:3: this_RelationExpression_0= ruleRelationExpression ( () otherlv_2= '&' ( (lv_right_3_0= ruleRelationExpression ) ) )*
            {

            			newCompositeNode(grammarAccess.getAndExpressionAccess().getRelationExpressionParserRuleCall_0());
            		
            pushFollow(FOLLOW_16);
            this_RelationExpression_0=ruleRelationExpression();

            state._fsp--;


            			current = this_RelationExpression_0;
            			afterParserOrEnumRuleCall();
            		
            // InternalModel.g:897:3: ( () otherlv_2= '&' ( (lv_right_3_0= ruleRelationExpression ) ) )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==24) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // InternalModel.g:898:4: () otherlv_2= '&' ( (lv_right_3_0= ruleRelationExpression ) )
            	    {
            	    // InternalModel.g:898:4: ()
            	    // InternalModel.g:899:5: 
            	    {

            	    					current = forceCreateModelElementAndSet(
            	    						grammarAccess.getAndExpressionAccess().getAndExpressionLeftAction_1_0(),
            	    						current);
            	    				

            	    }

            	    otherlv_2=(Token)match(input,24,FOLLOW_8); 

            	    				newLeafNode(otherlv_2, grammarAccess.getAndExpressionAccess().getAmpersandKeyword_1_1());
            	    			
            	    // InternalModel.g:909:4: ( (lv_right_3_0= ruleRelationExpression ) )
            	    // InternalModel.g:910:5: (lv_right_3_0= ruleRelationExpression )
            	    {
            	    // InternalModel.g:910:5: (lv_right_3_0= ruleRelationExpression )
            	    // InternalModel.g:911:6: lv_right_3_0= ruleRelationExpression
            	    {

            	    						newCompositeNode(grammarAccess.getAndExpressionAccess().getRightRelationExpressionParserRuleCall_1_2_0());
            	    					
            	    pushFollow(FOLLOW_16);
            	    lv_right_3_0=ruleRelationExpression();

            	    state._fsp--;


            	    						if (current==null) {
            	    							current = createModelElementForParent(grammarAccess.getAndExpressionRule());
            	    						}
            	    						set(
            	    							current,
            	    							"right",
            	    							lv_right_3_0,
            	    							"quasylab.sibilla.lang.pm.Model.RelationExpression");
            	    						afterParserOrEnumRuleCall();
            	    					

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAndExpression"


    // $ANTLR start "entryRuleRelationExpression"
    // InternalModel.g:933:1: entryRuleRelationExpression returns [EObject current=null] : iv_ruleRelationExpression= ruleRelationExpression EOF ;
    public final EObject entryRuleRelationExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRelationExpression = null;


        try {
            // InternalModel.g:933:59: (iv_ruleRelationExpression= ruleRelationExpression EOF )
            // InternalModel.g:934:2: iv_ruleRelationExpression= ruleRelationExpression EOF
            {
             newCompositeNode(grammarAccess.getRelationExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRelationExpression=ruleRelationExpression();

            state._fsp--;

             current =iv_ruleRelationExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRelationExpression"


    // $ANTLR start "ruleRelationExpression"
    // InternalModel.g:940:1: ruleRelationExpression returns [EObject current=null] : (this_SumDiffExpression_0= ruleSumDiffExpression ( () ( (lv_op_2_0= ruleRelationOperator ) ) ( (lv_right_3_0= ruleSumDiffExpression ) ) )? ) ;
    public final EObject ruleRelationExpression() throws RecognitionException {
        EObject current = null;

        EObject this_SumDiffExpression_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:946:2: ( (this_SumDiffExpression_0= ruleSumDiffExpression ( () ( (lv_op_2_0= ruleRelationOperator ) ) ( (lv_right_3_0= ruleSumDiffExpression ) ) )? ) )
            // InternalModel.g:947:2: (this_SumDiffExpression_0= ruleSumDiffExpression ( () ( (lv_op_2_0= ruleRelationOperator ) ) ( (lv_right_3_0= ruleSumDiffExpression ) ) )? )
            {
            // InternalModel.g:947:2: (this_SumDiffExpression_0= ruleSumDiffExpression ( () ( (lv_op_2_0= ruleRelationOperator ) ) ( (lv_right_3_0= ruleSumDiffExpression ) ) )? )
            // InternalModel.g:948:3: this_SumDiffExpression_0= ruleSumDiffExpression ( () ( (lv_op_2_0= ruleRelationOperator ) ) ( (lv_right_3_0= ruleSumDiffExpression ) ) )?
            {

            			newCompositeNode(grammarAccess.getRelationExpressionAccess().getSumDiffExpressionParserRuleCall_0());
            		
            pushFollow(FOLLOW_17);
            this_SumDiffExpression_0=ruleSumDiffExpression();

            state._fsp--;


            			current = this_SumDiffExpression_0;
            			afterParserOrEnumRuleCall();
            		
            // InternalModel.g:956:3: ( () ( (lv_op_2_0= ruleRelationOperator ) ) ( (lv_right_3_0= ruleSumDiffExpression ) ) )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( ((LA9_0>=25 && LA9_0<=30)) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // InternalModel.g:957:4: () ( (lv_op_2_0= ruleRelationOperator ) ) ( (lv_right_3_0= ruleSumDiffExpression ) )
                    {
                    // InternalModel.g:957:4: ()
                    // InternalModel.g:958:5: 
                    {

                    					current = forceCreateModelElementAndSet(
                    						grammarAccess.getRelationExpressionAccess().getRelationExpressionLeftAction_1_0(),
                    						current);
                    				

                    }

                    // InternalModel.g:964:4: ( (lv_op_2_0= ruleRelationOperator ) )
                    // InternalModel.g:965:5: (lv_op_2_0= ruleRelationOperator )
                    {
                    // InternalModel.g:965:5: (lv_op_2_0= ruleRelationOperator )
                    // InternalModel.g:966:6: lv_op_2_0= ruleRelationOperator
                    {

                    						newCompositeNode(grammarAccess.getRelationExpressionAccess().getOpRelationOperatorParserRuleCall_1_1_0());
                    					
                    pushFollow(FOLLOW_8);
                    lv_op_2_0=ruleRelationOperator();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getRelationExpressionRule());
                    						}
                    						set(
                    							current,
                    							"op",
                    							lv_op_2_0,
                    							"quasylab.sibilla.lang.pm.Model.RelationOperator");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }

                    // InternalModel.g:983:4: ( (lv_right_3_0= ruleSumDiffExpression ) )
                    // InternalModel.g:984:5: (lv_right_3_0= ruleSumDiffExpression )
                    {
                    // InternalModel.g:984:5: (lv_right_3_0= ruleSumDiffExpression )
                    // InternalModel.g:985:6: lv_right_3_0= ruleSumDiffExpression
                    {

                    						newCompositeNode(grammarAccess.getRelationExpressionAccess().getRightSumDiffExpressionParserRuleCall_1_2_0());
                    					
                    pushFollow(FOLLOW_2);
                    lv_right_3_0=ruleSumDiffExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getRelationExpressionRule());
                    						}
                    						set(
                    							current,
                    							"right",
                    							lv_right_3_0,
                    							"quasylab.sibilla.lang.pm.Model.SumDiffExpression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }


                    }
                    break;

            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationExpression"


    // $ANTLR start "entryRuleRelationOperator"
    // InternalModel.g:1007:1: entryRuleRelationOperator returns [EObject current=null] : iv_ruleRelationOperator= ruleRelationOperator EOF ;
    public final EObject entryRuleRelationOperator() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRelationOperator = null;


        try {
            // InternalModel.g:1007:57: (iv_ruleRelationOperator= ruleRelationOperator EOF )
            // InternalModel.g:1008:2: iv_ruleRelationOperator= ruleRelationOperator EOF
            {
             newCompositeNode(grammarAccess.getRelationOperatorRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRelationOperator=ruleRelationOperator();

            state._fsp--;

             current =iv_ruleRelationOperator; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRelationOperator"


    // $ANTLR start "ruleRelationOperator"
    // InternalModel.g:1014:1: ruleRelationOperator returns [EObject current=null] : ( ( () otherlv_1= '<' ) | ( () otherlv_3= '<=' ) | ( () otherlv_5= '==' ) | ( () otherlv_7= '!=' ) | ( () otherlv_9= '>' ) | ( () otherlv_11= '>=' ) ) ;
    public final EObject ruleRelationOperator() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token otherlv_11=null;


        	enterRule();

        try {
            // InternalModel.g:1020:2: ( ( ( () otherlv_1= '<' ) | ( () otherlv_3= '<=' ) | ( () otherlv_5= '==' ) | ( () otherlv_7= '!=' ) | ( () otherlv_9= '>' ) | ( () otherlv_11= '>=' ) ) )
            // InternalModel.g:1021:2: ( ( () otherlv_1= '<' ) | ( () otherlv_3= '<=' ) | ( () otherlv_5= '==' ) | ( () otherlv_7= '!=' ) | ( () otherlv_9= '>' ) | ( () otherlv_11= '>=' ) )
            {
            // InternalModel.g:1021:2: ( ( () otherlv_1= '<' ) | ( () otherlv_3= '<=' ) | ( () otherlv_5= '==' ) | ( () otherlv_7= '!=' ) | ( () otherlv_9= '>' ) | ( () otherlv_11= '>=' ) )
            int alt10=6;
            switch ( input.LA(1) ) {
            case 25:
                {
                alt10=1;
                }
                break;
            case 26:
                {
                alt10=2;
                }
                break;
            case 27:
                {
                alt10=3;
                }
                break;
            case 28:
                {
                alt10=4;
                }
                break;
            case 29:
                {
                alt10=5;
                }
                break;
            case 30:
                {
                alt10=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }

            switch (alt10) {
                case 1 :
                    // InternalModel.g:1022:3: ( () otherlv_1= '<' )
                    {
                    // InternalModel.g:1022:3: ( () otherlv_1= '<' )
                    // InternalModel.g:1023:4: () otherlv_1= '<'
                    {
                    // InternalModel.g:1023:4: ()
                    // InternalModel.g:1024:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getRelationOperatorAccess().getLessOperatorAction_0_0(),
                    						current);
                    				

                    }

                    otherlv_1=(Token)match(input,25,FOLLOW_2); 

                    				newLeafNode(otherlv_1, grammarAccess.getRelationOperatorAccess().getLessThanSignKeyword_0_1());
                    			

                    }


                    }
                    break;
                case 2 :
                    // InternalModel.g:1036:3: ( () otherlv_3= '<=' )
                    {
                    // InternalModel.g:1036:3: ( () otherlv_3= '<=' )
                    // InternalModel.g:1037:4: () otherlv_3= '<='
                    {
                    // InternalModel.g:1037:4: ()
                    // InternalModel.g:1038:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getRelationOperatorAccess().getLessOrEqualOperatorAction_1_0(),
                    						current);
                    				

                    }

                    otherlv_3=(Token)match(input,26,FOLLOW_2); 

                    				newLeafNode(otherlv_3, grammarAccess.getRelationOperatorAccess().getLessThanSignEqualsSignKeyword_1_1());
                    			

                    }


                    }
                    break;
                case 3 :
                    // InternalModel.g:1050:3: ( () otherlv_5= '==' )
                    {
                    // InternalModel.g:1050:3: ( () otherlv_5= '==' )
                    // InternalModel.g:1051:4: () otherlv_5= '=='
                    {
                    // InternalModel.g:1051:4: ()
                    // InternalModel.g:1052:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getRelationOperatorAccess().getEqualOperatorAction_2_0(),
                    						current);
                    				

                    }

                    otherlv_5=(Token)match(input,27,FOLLOW_2); 

                    				newLeafNode(otherlv_5, grammarAccess.getRelationOperatorAccess().getEqualsSignEqualsSignKeyword_2_1());
                    			

                    }


                    }
                    break;
                case 4 :
                    // InternalModel.g:1064:3: ( () otherlv_7= '!=' )
                    {
                    // InternalModel.g:1064:3: ( () otherlv_7= '!=' )
                    // InternalModel.g:1065:4: () otherlv_7= '!='
                    {
                    // InternalModel.g:1065:4: ()
                    // InternalModel.g:1066:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getRelationOperatorAccess().getDisEqualOperatorAction_3_0(),
                    						current);
                    				

                    }

                    otherlv_7=(Token)match(input,28,FOLLOW_2); 

                    				newLeafNode(otherlv_7, grammarAccess.getRelationOperatorAccess().getExclamationMarkEqualsSignKeyword_3_1());
                    			

                    }


                    }
                    break;
                case 5 :
                    // InternalModel.g:1078:3: ( () otherlv_9= '>' )
                    {
                    // InternalModel.g:1078:3: ( () otherlv_9= '>' )
                    // InternalModel.g:1079:4: () otherlv_9= '>'
                    {
                    // InternalModel.g:1079:4: ()
                    // InternalModel.g:1080:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getRelationOperatorAccess().getGreaterOperatorAction_4_0(),
                    						current);
                    				

                    }

                    otherlv_9=(Token)match(input,29,FOLLOW_2); 

                    				newLeafNode(otherlv_9, grammarAccess.getRelationOperatorAccess().getGreaterThanSignKeyword_4_1());
                    			

                    }


                    }
                    break;
                case 6 :
                    // InternalModel.g:1092:3: ( () otherlv_11= '>=' )
                    {
                    // InternalModel.g:1092:3: ( () otherlv_11= '>=' )
                    // InternalModel.g:1093:4: () otherlv_11= '>='
                    {
                    // InternalModel.g:1093:4: ()
                    // InternalModel.g:1094:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getRelationOperatorAccess().getGreaterOrEqualOperatorAction_5_0(),
                    						current);
                    				

                    }

                    otherlv_11=(Token)match(input,30,FOLLOW_2); 

                    				newLeafNode(otherlv_11, grammarAccess.getRelationOperatorAccess().getGreaterThanSignEqualsSignKeyword_5_1());
                    			

                    }


                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRelationOperator"


    // $ANTLR start "entryRuleMulDivExpression"
    // InternalModel.g:1109:1: entryRuleMulDivExpression returns [EObject current=null] : iv_ruleMulDivExpression= ruleMulDivExpression EOF ;
    public final EObject entryRuleMulDivExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMulDivExpression = null;


        try {
            // InternalModel.g:1109:57: (iv_ruleMulDivExpression= ruleMulDivExpression EOF )
            // InternalModel.g:1110:2: iv_ruleMulDivExpression= ruleMulDivExpression EOF
            {
             newCompositeNode(grammarAccess.getMulDivExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleMulDivExpression=ruleMulDivExpression();

            state._fsp--;

             current =iv_ruleMulDivExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMulDivExpression"


    // $ANTLR start "ruleMulDivExpression"
    // InternalModel.g:1116:1: ruleMulDivExpression returns [EObject current=null] : (this_ModuloExpression_0= ruleModuloExpression ( () ( (lv_op_2_0= ruleMulDivOperator ) ) ( (lv_right_3_0= ruleModuloExpression ) ) )* ) ;
    public final EObject ruleMulDivExpression() throws RecognitionException {
        EObject current = null;

        EObject this_ModuloExpression_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:1122:2: ( (this_ModuloExpression_0= ruleModuloExpression ( () ( (lv_op_2_0= ruleMulDivOperator ) ) ( (lv_right_3_0= ruleModuloExpression ) ) )* ) )
            // InternalModel.g:1123:2: (this_ModuloExpression_0= ruleModuloExpression ( () ( (lv_op_2_0= ruleMulDivOperator ) ) ( (lv_right_3_0= ruleModuloExpression ) ) )* )
            {
            // InternalModel.g:1123:2: (this_ModuloExpression_0= ruleModuloExpression ( () ( (lv_op_2_0= ruleMulDivOperator ) ) ( (lv_right_3_0= ruleModuloExpression ) ) )* )
            // InternalModel.g:1124:3: this_ModuloExpression_0= ruleModuloExpression ( () ( (lv_op_2_0= ruleMulDivOperator ) ) ( (lv_right_3_0= ruleModuloExpression ) ) )*
            {

            			newCompositeNode(grammarAccess.getMulDivExpressionAccess().getModuloExpressionParserRuleCall_0());
            		
            pushFollow(FOLLOW_18);
            this_ModuloExpression_0=ruleModuloExpression();

            state._fsp--;


            			current = this_ModuloExpression_0;
            			afterParserOrEnumRuleCall();
            		
            // InternalModel.g:1132:3: ( () ( (lv_op_2_0= ruleMulDivOperator ) ) ( (lv_right_3_0= ruleModuloExpression ) ) )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>=31 && LA11_0<=33)) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // InternalModel.g:1133:4: () ( (lv_op_2_0= ruleMulDivOperator ) ) ( (lv_right_3_0= ruleModuloExpression ) )
            	    {
            	    // InternalModel.g:1133:4: ()
            	    // InternalModel.g:1134:5: 
            	    {

            	    					current = forceCreateModelElementAndSet(
            	    						grammarAccess.getMulDivExpressionAccess().getMulDivExpressionLeftAction_1_0(),
            	    						current);
            	    				

            	    }

            	    // InternalModel.g:1140:4: ( (lv_op_2_0= ruleMulDivOperator ) )
            	    // InternalModel.g:1141:5: (lv_op_2_0= ruleMulDivOperator )
            	    {
            	    // InternalModel.g:1141:5: (lv_op_2_0= ruleMulDivOperator )
            	    // InternalModel.g:1142:6: lv_op_2_0= ruleMulDivOperator
            	    {

            	    						newCompositeNode(grammarAccess.getMulDivExpressionAccess().getOpMulDivOperatorParserRuleCall_1_1_0());
            	    					
            	    pushFollow(FOLLOW_8);
            	    lv_op_2_0=ruleMulDivOperator();

            	    state._fsp--;


            	    						if (current==null) {
            	    							current = createModelElementForParent(grammarAccess.getMulDivExpressionRule());
            	    						}
            	    						set(
            	    							current,
            	    							"op",
            	    							lv_op_2_0,
            	    							"quasylab.sibilla.lang.pm.Model.MulDivOperator");
            	    						afterParserOrEnumRuleCall();
            	    					

            	    }


            	    }

            	    // InternalModel.g:1159:4: ( (lv_right_3_0= ruleModuloExpression ) )
            	    // InternalModel.g:1160:5: (lv_right_3_0= ruleModuloExpression )
            	    {
            	    // InternalModel.g:1160:5: (lv_right_3_0= ruleModuloExpression )
            	    // InternalModel.g:1161:6: lv_right_3_0= ruleModuloExpression
            	    {

            	    						newCompositeNode(grammarAccess.getMulDivExpressionAccess().getRightModuloExpressionParserRuleCall_1_2_0());
            	    					
            	    pushFollow(FOLLOW_18);
            	    lv_right_3_0=ruleModuloExpression();

            	    state._fsp--;


            	    						if (current==null) {
            	    							current = createModelElementForParent(grammarAccess.getMulDivExpressionRule());
            	    						}
            	    						set(
            	    							current,
            	    							"right",
            	    							lv_right_3_0,
            	    							"quasylab.sibilla.lang.pm.Model.ModuloExpression");
            	    						afterParserOrEnumRuleCall();
            	    					

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMulDivExpression"


    // $ANTLR start "entryRuleMulDivOperator"
    // InternalModel.g:1183:1: entryRuleMulDivOperator returns [EObject current=null] : iv_ruleMulDivOperator= ruleMulDivOperator EOF ;
    public final EObject entryRuleMulDivOperator() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMulDivOperator = null;


        try {
            // InternalModel.g:1183:55: (iv_ruleMulDivOperator= ruleMulDivOperator EOF )
            // InternalModel.g:1184:2: iv_ruleMulDivOperator= ruleMulDivOperator EOF
            {
             newCompositeNode(grammarAccess.getMulDivOperatorRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleMulDivOperator=ruleMulDivOperator();

            state._fsp--;

             current =iv_ruleMulDivOperator; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMulDivOperator"


    // $ANTLR start "ruleMulDivOperator"
    // InternalModel.g:1190:1: ruleMulDivOperator returns [EObject current=null] : ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '//' ) ) ;
    public final EObject ruleMulDivOperator() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;
        Token otherlv_5=null;


        	enterRule();

        try {
            // InternalModel.g:1196:2: ( ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '//' ) ) )
            // InternalModel.g:1197:2: ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '//' ) )
            {
            // InternalModel.g:1197:2: ( ( () otherlv_1= '*' ) | ( () otherlv_3= '/' ) | ( () otherlv_5= '//' ) )
            int alt12=3;
            switch ( input.LA(1) ) {
            case 31:
                {
                alt12=1;
                }
                break;
            case 32:
                {
                alt12=2;
                }
                break;
            case 33:
                {
                alt12=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // InternalModel.g:1198:3: ( () otherlv_1= '*' )
                    {
                    // InternalModel.g:1198:3: ( () otherlv_1= '*' )
                    // InternalModel.g:1199:4: () otherlv_1= '*'
                    {
                    // InternalModel.g:1199:4: ()
                    // InternalModel.g:1200:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getMulDivOperatorAccess().getMultiplicationOperatorAction_0_0(),
                    						current);
                    				

                    }

                    otherlv_1=(Token)match(input,31,FOLLOW_2); 

                    				newLeafNode(otherlv_1, grammarAccess.getMulDivOperatorAccess().getAsteriskKeyword_0_1());
                    			

                    }


                    }
                    break;
                case 2 :
                    // InternalModel.g:1212:3: ( () otherlv_3= '/' )
                    {
                    // InternalModel.g:1212:3: ( () otherlv_3= '/' )
                    // InternalModel.g:1213:4: () otherlv_3= '/'
                    {
                    // InternalModel.g:1213:4: ()
                    // InternalModel.g:1214:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getMulDivOperatorAccess().getDivisionOperatorAction_1_0(),
                    						current);
                    				

                    }

                    otherlv_3=(Token)match(input,32,FOLLOW_2); 

                    				newLeafNode(otherlv_3, grammarAccess.getMulDivOperatorAccess().getSolidusKeyword_1_1());
                    			

                    }


                    }
                    break;
                case 3 :
                    // InternalModel.g:1226:3: ( () otherlv_5= '//' )
                    {
                    // InternalModel.g:1226:3: ( () otherlv_5= '//' )
                    // InternalModel.g:1227:4: () otherlv_5= '//'
                    {
                    // InternalModel.g:1227:4: ()
                    // InternalModel.g:1228:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getMulDivOperatorAccess().getZeroDivisionOperatorAction_2_0(),
                    						current);
                    				

                    }

                    otherlv_5=(Token)match(input,33,FOLLOW_2); 

                    				newLeafNode(otherlv_5, grammarAccess.getMulDivOperatorAccess().getSolidusSolidusKeyword_2_1());
                    			

                    }


                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMulDivOperator"


    // $ANTLR start "entryRuleSumDiffExpression"
    // InternalModel.g:1243:1: entryRuleSumDiffExpression returns [EObject current=null] : iv_ruleSumDiffExpression= ruleSumDiffExpression EOF ;
    public final EObject entryRuleSumDiffExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSumDiffExpression = null;


        try {
            // InternalModel.g:1243:58: (iv_ruleSumDiffExpression= ruleSumDiffExpression EOF )
            // InternalModel.g:1244:2: iv_ruleSumDiffExpression= ruleSumDiffExpression EOF
            {
             newCompositeNode(grammarAccess.getSumDiffExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSumDiffExpression=ruleSumDiffExpression();

            state._fsp--;

             current =iv_ruleSumDiffExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSumDiffExpression"


    // $ANTLR start "ruleSumDiffExpression"
    // InternalModel.g:1250:1: ruleSumDiffExpression returns [EObject current=null] : (this_MulDivExpression_0= ruleMulDivExpression ( () ( (lv_op_2_0= ruleSumDiffOperator ) ) ( (lv_right_3_0= ruleMulDivExpression ) ) )* ) ;
    public final EObject ruleSumDiffExpression() throws RecognitionException {
        EObject current = null;

        EObject this_MulDivExpression_0 = null;

        EObject lv_op_2_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:1256:2: ( (this_MulDivExpression_0= ruleMulDivExpression ( () ( (lv_op_2_0= ruleSumDiffOperator ) ) ( (lv_right_3_0= ruleMulDivExpression ) ) )* ) )
            // InternalModel.g:1257:2: (this_MulDivExpression_0= ruleMulDivExpression ( () ( (lv_op_2_0= ruleSumDiffOperator ) ) ( (lv_right_3_0= ruleMulDivExpression ) ) )* )
            {
            // InternalModel.g:1257:2: (this_MulDivExpression_0= ruleMulDivExpression ( () ( (lv_op_2_0= ruleSumDiffOperator ) ) ( (lv_right_3_0= ruleMulDivExpression ) ) )* )
            // InternalModel.g:1258:3: this_MulDivExpression_0= ruleMulDivExpression ( () ( (lv_op_2_0= ruleSumDiffOperator ) ) ( (lv_right_3_0= ruleMulDivExpression ) ) )*
            {

            			newCompositeNode(grammarAccess.getSumDiffExpressionAccess().getMulDivExpressionParserRuleCall_0());
            		
            pushFollow(FOLLOW_19);
            this_MulDivExpression_0=ruleMulDivExpression();

            state._fsp--;


            			current = this_MulDivExpression_0;
            			afterParserOrEnumRuleCall();
            		
            // InternalModel.g:1266:3: ( () ( (lv_op_2_0= ruleSumDiffOperator ) ) ( (lv_right_3_0= ruleMulDivExpression ) ) )*
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( ((LA13_0>=34 && LA13_0<=35)) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // InternalModel.g:1267:4: () ( (lv_op_2_0= ruleSumDiffOperator ) ) ( (lv_right_3_0= ruleMulDivExpression ) )
            	    {
            	    // InternalModel.g:1267:4: ()
            	    // InternalModel.g:1268:5: 
            	    {

            	    					current = forceCreateModelElementAndSet(
            	    						grammarAccess.getSumDiffExpressionAccess().getSumDiffExpressionLeftAction_1_0(),
            	    						current);
            	    				

            	    }

            	    // InternalModel.g:1274:4: ( (lv_op_2_0= ruleSumDiffOperator ) )
            	    // InternalModel.g:1275:5: (lv_op_2_0= ruleSumDiffOperator )
            	    {
            	    // InternalModel.g:1275:5: (lv_op_2_0= ruleSumDiffOperator )
            	    // InternalModel.g:1276:6: lv_op_2_0= ruleSumDiffOperator
            	    {

            	    						newCompositeNode(grammarAccess.getSumDiffExpressionAccess().getOpSumDiffOperatorParserRuleCall_1_1_0());
            	    					
            	    pushFollow(FOLLOW_8);
            	    lv_op_2_0=ruleSumDiffOperator();

            	    state._fsp--;


            	    						if (current==null) {
            	    							current = createModelElementForParent(grammarAccess.getSumDiffExpressionRule());
            	    						}
            	    						set(
            	    							current,
            	    							"op",
            	    							lv_op_2_0,
            	    							"quasylab.sibilla.lang.pm.Model.SumDiffOperator");
            	    						afterParserOrEnumRuleCall();
            	    					

            	    }


            	    }

            	    // InternalModel.g:1293:4: ( (lv_right_3_0= ruleMulDivExpression ) )
            	    // InternalModel.g:1294:5: (lv_right_3_0= ruleMulDivExpression )
            	    {
            	    // InternalModel.g:1294:5: (lv_right_3_0= ruleMulDivExpression )
            	    // InternalModel.g:1295:6: lv_right_3_0= ruleMulDivExpression
            	    {

            	    						newCompositeNode(grammarAccess.getSumDiffExpressionAccess().getRightMulDivExpressionParserRuleCall_1_2_0());
            	    					
            	    pushFollow(FOLLOW_19);
            	    lv_right_3_0=ruleMulDivExpression();

            	    state._fsp--;


            	    						if (current==null) {
            	    							current = createModelElementForParent(grammarAccess.getSumDiffExpressionRule());
            	    						}
            	    						set(
            	    							current,
            	    							"right",
            	    							lv_right_3_0,
            	    							"quasylab.sibilla.lang.pm.Model.MulDivExpression");
            	    						afterParserOrEnumRuleCall();
            	    					

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop13;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSumDiffExpression"


    // $ANTLR start "entryRuleSumDiffOperator"
    // InternalModel.g:1317:1: entryRuleSumDiffOperator returns [EObject current=null] : iv_ruleSumDiffOperator= ruleSumDiffOperator EOF ;
    public final EObject entryRuleSumDiffOperator() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSumDiffOperator = null;


        try {
            // InternalModel.g:1317:56: (iv_ruleSumDiffOperator= ruleSumDiffOperator EOF )
            // InternalModel.g:1318:2: iv_ruleSumDiffOperator= ruleSumDiffOperator EOF
            {
             newCompositeNode(grammarAccess.getSumDiffOperatorRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSumDiffOperator=ruleSumDiffOperator();

            state._fsp--;

             current =iv_ruleSumDiffOperator; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSumDiffOperator"


    // $ANTLR start "ruleSumDiffOperator"
    // InternalModel.g:1324:1: ruleSumDiffOperator returns [EObject current=null] : ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) ) ;
    public final EObject ruleSumDiffOperator() throws RecognitionException {
        EObject current = null;

        Token otherlv_1=null;
        Token otherlv_3=null;


        	enterRule();

        try {
            // InternalModel.g:1330:2: ( ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) ) )
            // InternalModel.g:1331:2: ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) )
            {
            // InternalModel.g:1331:2: ( ( () otherlv_1= '+' ) | ( () otherlv_3= '-' ) )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0==34) ) {
                alt14=1;
            }
            else if ( (LA14_0==35) ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // InternalModel.g:1332:3: ( () otherlv_1= '+' )
                    {
                    // InternalModel.g:1332:3: ( () otherlv_1= '+' )
                    // InternalModel.g:1333:4: () otherlv_1= '+'
                    {
                    // InternalModel.g:1333:4: ()
                    // InternalModel.g:1334:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getSumDiffOperatorAccess().getSumOperatorAction_0_0(),
                    						current);
                    				

                    }

                    otherlv_1=(Token)match(input,34,FOLLOW_2); 

                    				newLeafNode(otherlv_1, grammarAccess.getSumDiffOperatorAccess().getPlusSignKeyword_0_1());
                    			

                    }


                    }
                    break;
                case 2 :
                    // InternalModel.g:1346:3: ( () otherlv_3= '-' )
                    {
                    // InternalModel.g:1346:3: ( () otherlv_3= '-' )
                    // InternalModel.g:1347:4: () otherlv_3= '-'
                    {
                    // InternalModel.g:1347:4: ()
                    // InternalModel.g:1348:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getSumDiffOperatorAccess().getDifferenceOperatorAction_1_0(),
                    						current);
                    				

                    }

                    otherlv_3=(Token)match(input,35,FOLLOW_2); 

                    				newLeafNode(otherlv_3, grammarAccess.getSumDiffOperatorAccess().getHyphenMinusKeyword_1_1());
                    			

                    }


                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSumDiffOperator"


    // $ANTLR start "entryRuleModuloExpression"
    // InternalModel.g:1363:1: entryRuleModuloExpression returns [EObject current=null] : iv_ruleModuloExpression= ruleModuloExpression EOF ;
    public final EObject entryRuleModuloExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleModuloExpression = null;


        try {
            // InternalModel.g:1363:57: (iv_ruleModuloExpression= ruleModuloExpression EOF )
            // InternalModel.g:1364:2: iv_ruleModuloExpression= ruleModuloExpression EOF
            {
             newCompositeNode(grammarAccess.getModuloExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleModuloExpression=ruleModuloExpression();

            state._fsp--;

             current =iv_ruleModuloExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleModuloExpression"


    // $ANTLR start "ruleModuloExpression"
    // InternalModel.g:1370:1: ruleModuloExpression returns [EObject current=null] : (this_BaseExpression_0= ruleBaseExpression ( () otherlv_2= '%' ( (lv_right_3_0= ruleBaseExpression ) ) )* ) ;
    public final EObject ruleModuloExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        EObject this_BaseExpression_0 = null;

        EObject lv_right_3_0 = null;



        	enterRule();

        try {
            // InternalModel.g:1376:2: ( (this_BaseExpression_0= ruleBaseExpression ( () otherlv_2= '%' ( (lv_right_3_0= ruleBaseExpression ) ) )* ) )
            // InternalModel.g:1377:2: (this_BaseExpression_0= ruleBaseExpression ( () otherlv_2= '%' ( (lv_right_3_0= ruleBaseExpression ) ) )* )
            {
            // InternalModel.g:1377:2: (this_BaseExpression_0= ruleBaseExpression ( () otherlv_2= '%' ( (lv_right_3_0= ruleBaseExpression ) ) )* )
            // InternalModel.g:1378:3: this_BaseExpression_0= ruleBaseExpression ( () otherlv_2= '%' ( (lv_right_3_0= ruleBaseExpression ) ) )*
            {

            			newCompositeNode(grammarAccess.getModuloExpressionAccess().getBaseExpressionParserRuleCall_0());
            		
            pushFollow(FOLLOW_20);
            this_BaseExpression_0=ruleBaseExpression();

            state._fsp--;


            			current = this_BaseExpression_0;
            			afterParserOrEnumRuleCall();
            		
            // InternalModel.g:1386:3: ( () otherlv_2= '%' ( (lv_right_3_0= ruleBaseExpression ) ) )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==36) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // InternalModel.g:1387:4: () otherlv_2= '%' ( (lv_right_3_0= ruleBaseExpression ) )
            	    {
            	    // InternalModel.g:1387:4: ()
            	    // InternalModel.g:1388:5: 
            	    {

            	    					current = forceCreateModelElementAndSet(
            	    						grammarAccess.getModuloExpressionAccess().getModuloExpressionLeftAction_1_0(),
            	    						current);
            	    				

            	    }

            	    otherlv_2=(Token)match(input,36,FOLLOW_8); 

            	    				newLeafNode(otherlv_2, grammarAccess.getModuloExpressionAccess().getPercentSignKeyword_1_1());
            	    			
            	    // InternalModel.g:1398:4: ( (lv_right_3_0= ruleBaseExpression ) )
            	    // InternalModel.g:1399:5: (lv_right_3_0= ruleBaseExpression )
            	    {
            	    // InternalModel.g:1399:5: (lv_right_3_0= ruleBaseExpression )
            	    // InternalModel.g:1400:6: lv_right_3_0= ruleBaseExpression
            	    {

            	    						newCompositeNode(grammarAccess.getModuloExpressionAccess().getRightBaseExpressionParserRuleCall_1_2_0());
            	    					
            	    pushFollow(FOLLOW_20);
            	    lv_right_3_0=ruleBaseExpression();

            	    state._fsp--;


            	    						if (current==null) {
            	    							current = createModelElementForParent(grammarAccess.getModuloExpressionRule());
            	    						}
            	    						set(
            	    							current,
            	    							"right",
            	    							lv_right_3_0,
            	    							"quasylab.sibilla.lang.pm.Model.BaseExpression");
            	    						afterParserOrEnumRuleCall();
            	    					

            	    }


            	    }


            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleModuloExpression"


    // $ANTLR start "entryRuleBaseExpression"
    // InternalModel.g:1422:1: entryRuleBaseExpression returns [EObject current=null] : iv_ruleBaseExpression= ruleBaseExpression EOF ;
    public final EObject entryRuleBaseExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleBaseExpression = null;


        try {
            // InternalModel.g:1422:55: (iv_ruleBaseExpression= ruleBaseExpression EOF )
            // InternalModel.g:1423:2: iv_ruleBaseExpression= ruleBaseExpression EOF
            {
             newCompositeNode(grammarAccess.getBaseExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleBaseExpression=ruleBaseExpression();

            state._fsp--;

             current =iv_ruleBaseExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleBaseExpression"


    // $ANTLR start "ruleBaseExpression"
    // InternalModel.g:1429:1: ruleBaseExpression returns [EObject current=null] : (this_NumExpression_0= ruleNumExpression | ( () otherlv_2= 'true' ) | ( () otherlv_4= 'false' ) | ( () otherlv_6= '!' ( (lv_argument_7_0= ruleBaseExpression ) ) ) | this_FractionOf_8= ruleFractionOf | this_NumberOf_9= ruleNumberOf | this_IfThenElseExpression_10= ruleIfThenElseExpression | ( () otherlv_12= '-' ( (lv_argument_13_0= ruleBaseExpression ) ) ) | ( () ( (otherlv_15= RULE_ID ) ) ) | ( () otherlv_17= 'min' otherlv_18= '(' ( (lv_args_19_0= ruleExpression ) ) (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+ otherlv_22= ')' ) | ( () otherlv_24= 'max' otherlv_25= '(' ( (lv_args_26_0= ruleExpression ) ) (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+ otherlv_29= ')' ) ) ;
    public final EObject ruleBaseExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_2=null;
        Token otherlv_4=null;
        Token otherlv_6=null;
        Token otherlv_12=null;
        Token otherlv_15=null;
        Token otherlv_17=null;
        Token otherlv_18=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_24=null;
        Token otherlv_25=null;
        Token otherlv_27=null;
        Token otherlv_29=null;
        EObject this_NumExpression_0 = null;

        EObject lv_argument_7_0 = null;

        EObject this_FractionOf_8 = null;

        EObject this_NumberOf_9 = null;

        EObject this_IfThenElseExpression_10 = null;

        EObject lv_argument_13_0 = null;

        EObject lv_args_19_0 = null;

        EObject lv_args_21_0 = null;

        EObject lv_args_26_0 = null;

        EObject lv_args_28_0 = null;



        	enterRule();

        try {
            // InternalModel.g:1435:2: ( (this_NumExpression_0= ruleNumExpression | ( () otherlv_2= 'true' ) | ( () otherlv_4= 'false' ) | ( () otherlv_6= '!' ( (lv_argument_7_0= ruleBaseExpression ) ) ) | this_FractionOf_8= ruleFractionOf | this_NumberOf_9= ruleNumberOf | this_IfThenElseExpression_10= ruleIfThenElseExpression | ( () otherlv_12= '-' ( (lv_argument_13_0= ruleBaseExpression ) ) ) | ( () ( (otherlv_15= RULE_ID ) ) ) | ( () otherlv_17= 'min' otherlv_18= '(' ( (lv_args_19_0= ruleExpression ) ) (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+ otherlv_22= ')' ) | ( () otherlv_24= 'max' otherlv_25= '(' ( (lv_args_26_0= ruleExpression ) ) (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+ otherlv_29= ')' ) ) )
            // InternalModel.g:1436:2: (this_NumExpression_0= ruleNumExpression | ( () otherlv_2= 'true' ) | ( () otherlv_4= 'false' ) | ( () otherlv_6= '!' ( (lv_argument_7_0= ruleBaseExpression ) ) ) | this_FractionOf_8= ruleFractionOf | this_NumberOf_9= ruleNumberOf | this_IfThenElseExpression_10= ruleIfThenElseExpression | ( () otherlv_12= '-' ( (lv_argument_13_0= ruleBaseExpression ) ) ) | ( () ( (otherlv_15= RULE_ID ) ) ) | ( () otherlv_17= 'min' otherlv_18= '(' ( (lv_args_19_0= ruleExpression ) ) (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+ otherlv_22= ')' ) | ( () otherlv_24= 'max' otherlv_25= '(' ( (lv_args_26_0= ruleExpression ) ) (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+ otherlv_29= ')' ) )
            {
            // InternalModel.g:1436:2: (this_NumExpression_0= ruleNumExpression | ( () otherlv_2= 'true' ) | ( () otherlv_4= 'false' ) | ( () otherlv_6= '!' ( (lv_argument_7_0= ruleBaseExpression ) ) ) | this_FractionOf_8= ruleFractionOf | this_NumberOf_9= ruleNumberOf | this_IfThenElseExpression_10= ruleIfThenElseExpression | ( () otherlv_12= '-' ( (lv_argument_13_0= ruleBaseExpression ) ) ) | ( () ( (otherlv_15= RULE_ID ) ) ) | ( () otherlv_17= 'min' otherlv_18= '(' ( (lv_args_19_0= ruleExpression ) ) (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+ otherlv_22= ')' ) | ( () otherlv_24= 'max' otherlv_25= '(' ( (lv_args_26_0= ruleExpression ) ) (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+ otherlv_29= ')' ) )
            int alt18=11;
            switch ( input.LA(1) ) {
            case RULE_INT:
            case 48:
                {
                alt18=1;
                }
                break;
            case 37:
                {
                alt18=2;
                }
                break;
            case 38:
                {
                alt18=3;
                }
                break;
            case 39:
                {
                alt18=4;
                }
                break;
            case 36:
                {
                alt18=5;
                }
                break;
            case 47:
                {
                alt18=6;
                }
                break;
            case 41:
                {
                alt18=7;
                }
                break;
            case 35:
                {
                alt18=8;
                }
                break;
            case RULE_ID:
                {
                alt18=9;
                }
                break;
            case 40:
                {
                alt18=10;
                }
                break;
            case 44:
                {
                alt18=11;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // InternalModel.g:1437:3: this_NumExpression_0= ruleNumExpression
                    {

                    			newCompositeNode(grammarAccess.getBaseExpressionAccess().getNumExpressionParserRuleCall_0());
                    		
                    pushFollow(FOLLOW_2);
                    this_NumExpression_0=ruleNumExpression();

                    state._fsp--;


                    			current = this_NumExpression_0;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 2 :
                    // InternalModel.g:1446:3: ( () otherlv_2= 'true' )
                    {
                    // InternalModel.g:1446:3: ( () otherlv_2= 'true' )
                    // InternalModel.g:1447:4: () otherlv_2= 'true'
                    {
                    // InternalModel.g:1447:4: ()
                    // InternalModel.g:1448:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getBaseExpressionAccess().getTrueLiteralAction_1_0(),
                    						current);
                    				

                    }

                    otherlv_2=(Token)match(input,37,FOLLOW_2); 

                    				newLeafNode(otherlv_2, grammarAccess.getBaseExpressionAccess().getTrueKeyword_1_1());
                    			

                    }


                    }
                    break;
                case 3 :
                    // InternalModel.g:1460:3: ( () otherlv_4= 'false' )
                    {
                    // InternalModel.g:1460:3: ( () otherlv_4= 'false' )
                    // InternalModel.g:1461:4: () otherlv_4= 'false'
                    {
                    // InternalModel.g:1461:4: ()
                    // InternalModel.g:1462:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getBaseExpressionAccess().getFalseLiteralAction_2_0(),
                    						current);
                    				

                    }

                    otherlv_4=(Token)match(input,38,FOLLOW_2); 

                    				newLeafNode(otherlv_4, grammarAccess.getBaseExpressionAccess().getFalseKeyword_2_1());
                    			

                    }


                    }
                    break;
                case 4 :
                    // InternalModel.g:1474:3: ( () otherlv_6= '!' ( (lv_argument_7_0= ruleBaseExpression ) ) )
                    {
                    // InternalModel.g:1474:3: ( () otherlv_6= '!' ( (lv_argument_7_0= ruleBaseExpression ) ) )
                    // InternalModel.g:1475:4: () otherlv_6= '!' ( (lv_argument_7_0= ruleBaseExpression ) )
                    {
                    // InternalModel.g:1475:4: ()
                    // InternalModel.g:1476:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getBaseExpressionAccess().getNotExpressionAction_3_0(),
                    						current);
                    				

                    }

                    otherlv_6=(Token)match(input,39,FOLLOW_8); 

                    				newLeafNode(otherlv_6, grammarAccess.getBaseExpressionAccess().getExclamationMarkKeyword_3_1());
                    			
                    // InternalModel.g:1486:4: ( (lv_argument_7_0= ruleBaseExpression ) )
                    // InternalModel.g:1487:5: (lv_argument_7_0= ruleBaseExpression )
                    {
                    // InternalModel.g:1487:5: (lv_argument_7_0= ruleBaseExpression )
                    // InternalModel.g:1488:6: lv_argument_7_0= ruleBaseExpression
                    {

                    						newCompositeNode(grammarAccess.getBaseExpressionAccess().getArgumentBaseExpressionParserRuleCall_3_2_0());
                    					
                    pushFollow(FOLLOW_2);
                    lv_argument_7_0=ruleBaseExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getBaseExpressionRule());
                    						}
                    						set(
                    							current,
                    							"argument",
                    							lv_argument_7_0,
                    							"quasylab.sibilla.lang.pm.Model.BaseExpression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 5 :
                    // InternalModel.g:1507:3: this_FractionOf_8= ruleFractionOf
                    {

                    			newCompositeNode(grammarAccess.getBaseExpressionAccess().getFractionOfParserRuleCall_4());
                    		
                    pushFollow(FOLLOW_2);
                    this_FractionOf_8=ruleFractionOf();

                    state._fsp--;


                    			current = this_FractionOf_8;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 6 :
                    // InternalModel.g:1516:3: this_NumberOf_9= ruleNumberOf
                    {

                    			newCompositeNode(grammarAccess.getBaseExpressionAccess().getNumberOfParserRuleCall_5());
                    		
                    pushFollow(FOLLOW_2);
                    this_NumberOf_9=ruleNumberOf();

                    state._fsp--;


                    			current = this_NumberOf_9;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 7 :
                    // InternalModel.g:1525:3: this_IfThenElseExpression_10= ruleIfThenElseExpression
                    {

                    			newCompositeNode(grammarAccess.getBaseExpressionAccess().getIfThenElseExpressionParserRuleCall_6());
                    		
                    pushFollow(FOLLOW_2);
                    this_IfThenElseExpression_10=ruleIfThenElseExpression();

                    state._fsp--;


                    			current = this_IfThenElseExpression_10;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 8 :
                    // InternalModel.g:1534:3: ( () otherlv_12= '-' ( (lv_argument_13_0= ruleBaseExpression ) ) )
                    {
                    // InternalModel.g:1534:3: ( () otherlv_12= '-' ( (lv_argument_13_0= ruleBaseExpression ) ) )
                    // InternalModel.g:1535:4: () otherlv_12= '-' ( (lv_argument_13_0= ruleBaseExpression ) )
                    {
                    // InternalModel.g:1535:4: ()
                    // InternalModel.g:1536:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getBaseExpressionAccess().getNegationExpressionAction_7_0(),
                    						current);
                    				

                    }

                    otherlv_12=(Token)match(input,35,FOLLOW_8); 

                    				newLeafNode(otherlv_12, grammarAccess.getBaseExpressionAccess().getHyphenMinusKeyword_7_1());
                    			
                    // InternalModel.g:1546:4: ( (lv_argument_13_0= ruleBaseExpression ) )
                    // InternalModel.g:1547:5: (lv_argument_13_0= ruleBaseExpression )
                    {
                    // InternalModel.g:1547:5: (lv_argument_13_0= ruleBaseExpression )
                    // InternalModel.g:1548:6: lv_argument_13_0= ruleBaseExpression
                    {

                    						newCompositeNode(grammarAccess.getBaseExpressionAccess().getArgumentBaseExpressionParserRuleCall_7_2_0());
                    					
                    pushFollow(FOLLOW_2);
                    lv_argument_13_0=ruleBaseExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getBaseExpressionRule());
                    						}
                    						set(
                    							current,
                    							"argument",
                    							lv_argument_13_0,
                    							"quasylab.sibilla.lang.pm.Model.BaseExpression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }


                    }


                    }
                    break;
                case 9 :
                    // InternalModel.g:1567:3: ( () ( (otherlv_15= RULE_ID ) ) )
                    {
                    // InternalModel.g:1567:3: ( () ( (otherlv_15= RULE_ID ) ) )
                    // InternalModel.g:1568:4: () ( (otherlv_15= RULE_ID ) )
                    {
                    // InternalModel.g:1568:4: ()
                    // InternalModel.g:1569:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getBaseExpressionAccess().getCallExpressionAction_8_0(),
                    						current);
                    				

                    }

                    // InternalModel.g:1575:4: ( (otherlv_15= RULE_ID ) )
                    // InternalModel.g:1576:5: (otherlv_15= RULE_ID )
                    {
                    // InternalModel.g:1576:5: (otherlv_15= RULE_ID )
                    // InternalModel.g:1577:6: otherlv_15= RULE_ID
                    {

                    						if (current==null) {
                    							current = createModelElement(grammarAccess.getBaseExpressionRule());
                    						}
                    					
                    otherlv_15=(Token)match(input,RULE_ID,FOLLOW_2); 

                    						newLeafNode(otherlv_15, grammarAccess.getBaseExpressionAccess().getSymbolReferenceableElementCrossReference_8_1_0());
                    					

                    }


                    }


                    }


                    }
                    break;
                case 10 :
                    // InternalModel.g:1590:3: ( () otherlv_17= 'min' otherlv_18= '(' ( (lv_args_19_0= ruleExpression ) ) (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+ otherlv_22= ')' )
                    {
                    // InternalModel.g:1590:3: ( () otherlv_17= 'min' otherlv_18= '(' ( (lv_args_19_0= ruleExpression ) ) (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+ otherlv_22= ')' )
                    // InternalModel.g:1591:4: () otherlv_17= 'min' otherlv_18= '(' ( (lv_args_19_0= ruleExpression ) ) (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+ otherlv_22= ')'
                    {
                    // InternalModel.g:1591:4: ()
                    // InternalModel.g:1592:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getBaseExpressionAccess().getMinExpressionAction_9_0(),
                    						current);
                    				

                    }

                    otherlv_17=(Token)match(input,40,FOLLOW_21); 

                    				newLeafNode(otherlv_17, grammarAccess.getBaseExpressionAccess().getMinKeyword_9_1());
                    			
                    otherlv_18=(Token)match(input,41,FOLLOW_8); 

                    				newLeafNode(otherlv_18, grammarAccess.getBaseExpressionAccess().getLeftParenthesisKeyword_9_2());
                    			
                    // InternalModel.g:1606:4: ( (lv_args_19_0= ruleExpression ) )
                    // InternalModel.g:1607:5: (lv_args_19_0= ruleExpression )
                    {
                    // InternalModel.g:1607:5: (lv_args_19_0= ruleExpression )
                    // InternalModel.g:1608:6: lv_args_19_0= ruleExpression
                    {

                    						newCompositeNode(grammarAccess.getBaseExpressionAccess().getArgsExpressionParserRuleCall_9_3_0());
                    					
                    pushFollow(FOLLOW_22);
                    lv_args_19_0=ruleExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getBaseExpressionRule());
                    						}
                    						add(
                    							current,
                    							"args",
                    							lv_args_19_0,
                    							"quasylab.sibilla.lang.pm.Model.Expression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }

                    // InternalModel.g:1625:4: (otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) ) )+
                    int cnt16=0;
                    loop16:
                    do {
                        int alt16=2;
                        int LA16_0 = input.LA(1);

                        if ( (LA16_0==42) ) {
                            alt16=1;
                        }


                        switch (alt16) {
                    	case 1 :
                    	    // InternalModel.g:1626:5: otherlv_20= ',' ( (lv_args_21_0= ruleExpression ) )
                    	    {
                    	    otherlv_20=(Token)match(input,42,FOLLOW_8); 

                    	    					newLeafNode(otherlv_20, grammarAccess.getBaseExpressionAccess().getCommaKeyword_9_4_0());
                    	    				
                    	    // InternalModel.g:1630:5: ( (lv_args_21_0= ruleExpression ) )
                    	    // InternalModel.g:1631:6: (lv_args_21_0= ruleExpression )
                    	    {
                    	    // InternalModel.g:1631:6: (lv_args_21_0= ruleExpression )
                    	    // InternalModel.g:1632:7: lv_args_21_0= ruleExpression
                    	    {

                    	    							newCompositeNode(grammarAccess.getBaseExpressionAccess().getArgsExpressionParserRuleCall_9_4_1_0());
                    	    						
                    	    pushFollow(FOLLOW_23);
                    	    lv_args_21_0=ruleExpression();

                    	    state._fsp--;


                    	    							if (current==null) {
                    	    								current = createModelElementForParent(grammarAccess.getBaseExpressionRule());
                    	    							}
                    	    							add(
                    	    								current,
                    	    								"args",
                    	    								lv_args_21_0,
                    	    								"quasylab.sibilla.lang.pm.Model.Expression");
                    	    							afterParserOrEnumRuleCall();
                    	    						

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt16 >= 1 ) break loop16;
                                EarlyExitException eee =
                                    new EarlyExitException(16, input);
                                throw eee;
                        }
                        cnt16++;
                    } while (true);

                    otherlv_22=(Token)match(input,43,FOLLOW_2); 

                    				newLeafNode(otherlv_22, grammarAccess.getBaseExpressionAccess().getRightParenthesisKeyword_9_5());
                    			

                    }


                    }
                    break;
                case 11 :
                    // InternalModel.g:1656:3: ( () otherlv_24= 'max' otherlv_25= '(' ( (lv_args_26_0= ruleExpression ) ) (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+ otherlv_29= ')' )
                    {
                    // InternalModel.g:1656:3: ( () otherlv_24= 'max' otherlv_25= '(' ( (lv_args_26_0= ruleExpression ) ) (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+ otherlv_29= ')' )
                    // InternalModel.g:1657:4: () otherlv_24= 'max' otherlv_25= '(' ( (lv_args_26_0= ruleExpression ) ) (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+ otherlv_29= ')'
                    {
                    // InternalModel.g:1657:4: ()
                    // InternalModel.g:1658:5: 
                    {

                    					current = forceCreateModelElement(
                    						grammarAccess.getBaseExpressionAccess().getMaxExpressionAction_10_0(),
                    						current);
                    				

                    }

                    otherlv_24=(Token)match(input,44,FOLLOW_21); 

                    				newLeafNode(otherlv_24, grammarAccess.getBaseExpressionAccess().getMaxKeyword_10_1());
                    			
                    otherlv_25=(Token)match(input,41,FOLLOW_8); 

                    				newLeafNode(otherlv_25, grammarAccess.getBaseExpressionAccess().getLeftParenthesisKeyword_10_2());
                    			
                    // InternalModel.g:1672:4: ( (lv_args_26_0= ruleExpression ) )
                    // InternalModel.g:1673:5: (lv_args_26_0= ruleExpression )
                    {
                    // InternalModel.g:1673:5: (lv_args_26_0= ruleExpression )
                    // InternalModel.g:1674:6: lv_args_26_0= ruleExpression
                    {

                    						newCompositeNode(grammarAccess.getBaseExpressionAccess().getArgsExpressionParserRuleCall_10_3_0());
                    					
                    pushFollow(FOLLOW_22);
                    lv_args_26_0=ruleExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getBaseExpressionRule());
                    						}
                    						add(
                    							current,
                    							"args",
                    							lv_args_26_0,
                    							"quasylab.sibilla.lang.pm.Model.Expression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }

                    // InternalModel.g:1691:4: (otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) ) )+
                    int cnt17=0;
                    loop17:
                    do {
                        int alt17=2;
                        int LA17_0 = input.LA(1);

                        if ( (LA17_0==42) ) {
                            alt17=1;
                        }


                        switch (alt17) {
                    	case 1 :
                    	    // InternalModel.g:1692:5: otherlv_27= ',' ( (lv_args_28_0= ruleExpression ) )
                    	    {
                    	    otherlv_27=(Token)match(input,42,FOLLOW_8); 

                    	    					newLeafNode(otherlv_27, grammarAccess.getBaseExpressionAccess().getCommaKeyword_10_4_0());
                    	    				
                    	    // InternalModel.g:1696:5: ( (lv_args_28_0= ruleExpression ) )
                    	    // InternalModel.g:1697:6: (lv_args_28_0= ruleExpression )
                    	    {
                    	    // InternalModel.g:1697:6: (lv_args_28_0= ruleExpression )
                    	    // InternalModel.g:1698:7: lv_args_28_0= ruleExpression
                    	    {

                    	    							newCompositeNode(grammarAccess.getBaseExpressionAccess().getArgsExpressionParserRuleCall_10_4_1_0());
                    	    						
                    	    pushFollow(FOLLOW_23);
                    	    lv_args_28_0=ruleExpression();

                    	    state._fsp--;


                    	    							if (current==null) {
                    	    								current = createModelElementForParent(grammarAccess.getBaseExpressionRule());
                    	    							}
                    	    							add(
                    	    								current,
                    	    								"args",
                    	    								lv_args_28_0,
                    	    								"quasylab.sibilla.lang.pm.Model.Expression");
                    	    							afterParserOrEnumRuleCall();
                    	    						

                    	    }


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt17 >= 1 ) break loop17;
                                EarlyExitException eee =
                                    new EarlyExitException(17, input);
                                throw eee;
                        }
                        cnt17++;
                    } while (true);

                    otherlv_29=(Token)match(input,43,FOLLOW_2); 

                    				newLeafNode(otherlv_29, grammarAccess.getBaseExpressionAccess().getRightParenthesisKeyword_10_5());
                    			

                    }


                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleBaseExpression"


    // $ANTLR start "entryRuleIfThenElseExpression"
    // InternalModel.g:1725:1: entryRuleIfThenElseExpression returns [EObject current=null] : iv_ruleIfThenElseExpression= ruleIfThenElseExpression EOF ;
    public final EObject entryRuleIfThenElseExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleIfThenElseExpression = null;


        try {
            // InternalModel.g:1725:61: (iv_ruleIfThenElseExpression= ruleIfThenElseExpression EOF )
            // InternalModel.g:1726:2: iv_ruleIfThenElseExpression= ruleIfThenElseExpression EOF
            {
             newCompositeNode(grammarAccess.getIfThenElseExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleIfThenElseExpression=ruleIfThenElseExpression();

            state._fsp--;

             current =iv_ruleIfThenElseExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleIfThenElseExpression"


    // $ANTLR start "ruleIfThenElseExpression"
    // InternalModel.g:1732:1: ruleIfThenElseExpression returns [EObject current=null] : (otherlv_0= '(' this_Expression_1= ruleExpression ( () otherlv_3= '?' ( (lv_ifBranch_4_0= ruleExpression ) ) otherlv_5= ':' ( (lv_elseBranch_6_0= ruleExpression ) ) )? otherlv_7= ')' ) ;
    public final EObject ruleIfThenElseExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_3=null;
        Token otherlv_5=null;
        Token otherlv_7=null;
        EObject this_Expression_1 = null;

        EObject lv_ifBranch_4_0 = null;

        EObject lv_elseBranch_6_0 = null;



        	enterRule();

        try {
            // InternalModel.g:1738:2: ( (otherlv_0= '(' this_Expression_1= ruleExpression ( () otherlv_3= '?' ( (lv_ifBranch_4_0= ruleExpression ) ) otherlv_5= ':' ( (lv_elseBranch_6_0= ruleExpression ) ) )? otherlv_7= ')' ) )
            // InternalModel.g:1739:2: (otherlv_0= '(' this_Expression_1= ruleExpression ( () otherlv_3= '?' ( (lv_ifBranch_4_0= ruleExpression ) ) otherlv_5= ':' ( (lv_elseBranch_6_0= ruleExpression ) ) )? otherlv_7= ')' )
            {
            // InternalModel.g:1739:2: (otherlv_0= '(' this_Expression_1= ruleExpression ( () otherlv_3= '?' ( (lv_ifBranch_4_0= ruleExpression ) ) otherlv_5= ':' ( (lv_elseBranch_6_0= ruleExpression ) ) )? otherlv_7= ')' )
            // InternalModel.g:1740:3: otherlv_0= '(' this_Expression_1= ruleExpression ( () otherlv_3= '?' ( (lv_ifBranch_4_0= ruleExpression ) ) otherlv_5= ':' ( (lv_elseBranch_6_0= ruleExpression ) ) )? otherlv_7= ')'
            {
            otherlv_0=(Token)match(input,41,FOLLOW_8); 

            			newLeafNode(otherlv_0, grammarAccess.getIfThenElseExpressionAccess().getLeftParenthesisKeyword_0());
            		

            			newCompositeNode(grammarAccess.getIfThenElseExpressionAccess().getExpressionParserRuleCall_1());
            		
            pushFollow(FOLLOW_24);
            this_Expression_1=ruleExpression();

            state._fsp--;


            			current = this_Expression_1;
            			afterParserOrEnumRuleCall();
            		
            // InternalModel.g:1752:3: ( () otherlv_3= '?' ( (lv_ifBranch_4_0= ruleExpression ) ) otherlv_5= ':' ( (lv_elseBranch_6_0= ruleExpression ) ) )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==45) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // InternalModel.g:1753:4: () otherlv_3= '?' ( (lv_ifBranch_4_0= ruleExpression ) ) otherlv_5= ':' ( (lv_elseBranch_6_0= ruleExpression ) )
                    {
                    // InternalModel.g:1753:4: ()
                    // InternalModel.g:1754:5: 
                    {

                    					current = forceCreateModelElementAndSet(
                    						grammarAccess.getIfThenElseExpressionAccess().getIfThenElseExpressionGuardAction_2_0(),
                    						current);
                    				

                    }

                    otherlv_3=(Token)match(input,45,FOLLOW_8); 

                    				newLeafNode(otherlv_3, grammarAccess.getIfThenElseExpressionAccess().getQuestionMarkKeyword_2_1());
                    			
                    // InternalModel.g:1764:4: ( (lv_ifBranch_4_0= ruleExpression ) )
                    // InternalModel.g:1765:5: (lv_ifBranch_4_0= ruleExpression )
                    {
                    // InternalModel.g:1765:5: (lv_ifBranch_4_0= ruleExpression )
                    // InternalModel.g:1766:6: lv_ifBranch_4_0= ruleExpression
                    {

                    						newCompositeNode(grammarAccess.getIfThenElseExpressionAccess().getIfBranchExpressionParserRuleCall_2_2_0());
                    					
                    pushFollow(FOLLOW_25);
                    lv_ifBranch_4_0=ruleExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getIfThenElseExpressionRule());
                    						}
                    						set(
                    							current,
                    							"ifBranch",
                    							lv_ifBranch_4_0,
                    							"quasylab.sibilla.lang.pm.Model.Expression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }

                    otherlv_5=(Token)match(input,46,FOLLOW_8); 

                    				newLeafNode(otherlv_5, grammarAccess.getIfThenElseExpressionAccess().getColonKeyword_2_3());
                    			
                    // InternalModel.g:1787:4: ( (lv_elseBranch_6_0= ruleExpression ) )
                    // InternalModel.g:1788:5: (lv_elseBranch_6_0= ruleExpression )
                    {
                    // InternalModel.g:1788:5: (lv_elseBranch_6_0= ruleExpression )
                    // InternalModel.g:1789:6: lv_elseBranch_6_0= ruleExpression
                    {

                    						newCompositeNode(grammarAccess.getIfThenElseExpressionAccess().getElseBranchExpressionParserRuleCall_2_4_0());
                    					
                    pushFollow(FOLLOW_26);
                    lv_elseBranch_6_0=ruleExpression();

                    state._fsp--;


                    						if (current==null) {
                    							current = createModelElementForParent(grammarAccess.getIfThenElseExpressionRule());
                    						}
                    						set(
                    							current,
                    							"elseBranch",
                    							lv_elseBranch_6_0,
                    							"quasylab.sibilla.lang.pm.Model.Expression");
                    						afterParserOrEnumRuleCall();
                    					

                    }


                    }


                    }
                    break;

            }

            otherlv_7=(Token)match(input,43,FOLLOW_2); 

            			newLeafNode(otherlv_7, grammarAccess.getIfThenElseExpressionAccess().getRightParenthesisKeyword_3());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleIfThenElseExpression"


    // $ANTLR start "entryRuleFractionOf"
    // InternalModel.g:1815:1: entryRuleFractionOf returns [EObject current=null] : iv_ruleFractionOf= ruleFractionOf EOF ;
    public final EObject entryRuleFractionOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFractionOf = null;


        try {
            // InternalModel.g:1815:51: (iv_ruleFractionOf= ruleFractionOf EOF )
            // InternalModel.g:1816:2: iv_ruleFractionOf= ruleFractionOf EOF
            {
             newCompositeNode(grammarAccess.getFractionOfRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleFractionOf=ruleFractionOf();

            state._fsp--;

             current =iv_ruleFractionOf; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFractionOf"


    // $ANTLR start "ruleFractionOf"
    // InternalModel.g:1822:1: ruleFractionOf returns [EObject current=null] : (otherlv_0= '%' ( (otherlv_1= RULE_ID ) ) ) ;
    public final EObject ruleFractionOf() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;


        	enterRule();

        try {
            // InternalModel.g:1828:2: ( (otherlv_0= '%' ( (otherlv_1= RULE_ID ) ) ) )
            // InternalModel.g:1829:2: (otherlv_0= '%' ( (otherlv_1= RULE_ID ) ) )
            {
            // InternalModel.g:1829:2: (otherlv_0= '%' ( (otherlv_1= RULE_ID ) ) )
            // InternalModel.g:1830:3: otherlv_0= '%' ( (otherlv_1= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,36,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getFractionOfAccess().getPercentSignKeyword_0());
            		
            // InternalModel.g:1834:3: ( (otherlv_1= RULE_ID ) )
            // InternalModel.g:1835:4: (otherlv_1= RULE_ID )
            {
            // InternalModel.g:1835:4: (otherlv_1= RULE_ID )
            // InternalModel.g:1836:5: otherlv_1= RULE_ID
            {

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFractionOfRule());
            					}
            				
            otherlv_1=(Token)match(input,RULE_ID,FOLLOW_2); 

            					newLeafNode(otherlv_1, grammarAccess.getFractionOfAccess().getAgentSpeciesCrossReference_1_0());
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFractionOf"


    // $ANTLR start "entryRuleNumberOf"
    // InternalModel.g:1851:1: entryRuleNumberOf returns [EObject current=null] : iv_ruleNumberOf= ruleNumberOf EOF ;
    public final EObject entryRuleNumberOf() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNumberOf = null;


        try {
            // InternalModel.g:1851:49: (iv_ruleNumberOf= ruleNumberOf EOF )
            // InternalModel.g:1852:2: iv_ruleNumberOf= ruleNumberOf EOF
            {
             newCompositeNode(grammarAccess.getNumberOfRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleNumberOf=ruleNumberOf();

            state._fsp--;

             current =iv_ruleNumberOf; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleNumberOf"


    // $ANTLR start "ruleNumberOf"
    // InternalModel.g:1858:1: ruleNumberOf returns [EObject current=null] : (otherlv_0= '#' ( (otherlv_1= RULE_ID ) ) ) ;
    public final EObject ruleNumberOf() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;


        	enterRule();

        try {
            // InternalModel.g:1864:2: ( (otherlv_0= '#' ( (otherlv_1= RULE_ID ) ) ) )
            // InternalModel.g:1865:2: (otherlv_0= '#' ( (otherlv_1= RULE_ID ) ) )
            {
            // InternalModel.g:1865:2: (otherlv_0= '#' ( (otherlv_1= RULE_ID ) ) )
            // InternalModel.g:1866:3: otherlv_0= '#' ( (otherlv_1= RULE_ID ) )
            {
            otherlv_0=(Token)match(input,47,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getNumberOfAccess().getNumberSignKeyword_0());
            		
            // InternalModel.g:1870:3: ( (otherlv_1= RULE_ID ) )
            // InternalModel.g:1871:4: (otherlv_1= RULE_ID )
            {
            // InternalModel.g:1871:4: (otherlv_1= RULE_ID )
            // InternalModel.g:1872:5: otherlv_1= RULE_ID
            {

            					if (current==null) {
            						current = createModelElement(grammarAccess.getNumberOfRule());
            					}
            				
            otherlv_1=(Token)match(input,RULE_ID,FOLLOW_2); 

            					newLeafNode(otherlv_1, grammarAccess.getNumberOfAccess().getAgentSpeciesCrossReference_1_0());
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleNumberOf"


    // $ANTLR start "entryRuleNumExpression"
    // InternalModel.g:1887:1: entryRuleNumExpression returns [EObject current=null] : iv_ruleNumExpression= ruleNumExpression EOF ;
    public final EObject entryRuleNumExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleNumExpression = null;


        try {
            // InternalModel.g:1887:54: (iv_ruleNumExpression= ruleNumExpression EOF )
            // InternalModel.g:1888:2: iv_ruleNumExpression= ruleNumExpression EOF
            {
             newCompositeNode(grammarAccess.getNumExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleNumExpression=ruleNumExpression();

            state._fsp--;

             current =iv_ruleNumExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleNumExpression"


    // $ANTLR start "ruleNumExpression"
    // InternalModel.g:1894:1: ruleNumExpression returns [EObject current=null] : ( ( ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? ) | ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) ) ) (otherlv_5= 'E' ( (lv_exponent_6_0= RULE_INT ) ) )? ) ;
    public final EObject ruleNumExpression() throws RecognitionException {
        EObject current = null;

        Token lv_intPart_0_0=null;
        Token lv_isReal_1_0=null;
        Token lv_decimalPart_2_0=null;
        Token lv_isReal_3_0=null;
        Token lv_decimalPart_4_0=null;
        Token otherlv_5=null;
        Token lv_exponent_6_0=null;


        	enterRule();

        try {
            // InternalModel.g:1900:2: ( ( ( ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? ) | ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) ) ) (otherlv_5= 'E' ( (lv_exponent_6_0= RULE_INT ) ) )? ) )
            // InternalModel.g:1901:2: ( ( ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? ) | ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) ) ) (otherlv_5= 'E' ( (lv_exponent_6_0= RULE_INT ) ) )? )
            {
            // InternalModel.g:1901:2: ( ( ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? ) | ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) ) ) (otherlv_5= 'E' ( (lv_exponent_6_0= RULE_INT ) ) )? )
            // InternalModel.g:1902:3: ( ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? ) | ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) ) ) (otherlv_5= 'E' ( (lv_exponent_6_0= RULE_INT ) ) )?
            {
            // InternalModel.g:1902:3: ( ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? ) | ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) ) )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==RULE_INT) ) {
                alt22=1;
            }
            else if ( (LA22_0==48) ) {
                alt22=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // InternalModel.g:1903:4: ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? )
                    {
                    // InternalModel.g:1903:4: ( ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )? )
                    // InternalModel.g:1904:5: ( (lv_intPart_0_0= RULE_INT ) ) ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )?
                    {
                    // InternalModel.g:1904:5: ( (lv_intPart_0_0= RULE_INT ) )
                    // InternalModel.g:1905:6: (lv_intPart_0_0= RULE_INT )
                    {
                    // InternalModel.g:1905:6: (lv_intPart_0_0= RULE_INT )
                    // InternalModel.g:1906:7: lv_intPart_0_0= RULE_INT
                    {
                    lv_intPart_0_0=(Token)match(input,RULE_INT,FOLLOW_27); 

                    							newLeafNode(lv_intPart_0_0, grammarAccess.getNumExpressionAccess().getIntPartINTTerminalRuleCall_0_0_0_0());
                    						

                    							if (current==null) {
                    								current = createModelElement(grammarAccess.getNumExpressionRule());
                    							}
                    							setWithLastConsumed(
                    								current,
                    								"intPart",
                    								lv_intPart_0_0,
                    								"org.eclipse.xtext.common.Terminals.INT");
                    						

                    }


                    }

                    // InternalModel.g:1922:5: ( ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )? )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( (LA21_0==48) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // InternalModel.g:1923:6: ( (lv_isReal_1_0= '.' ) ) ( (lv_decimalPart_2_0= RULE_INT ) )?
                            {
                            // InternalModel.g:1923:6: ( (lv_isReal_1_0= '.' ) )
                            // InternalModel.g:1924:7: (lv_isReal_1_0= '.' )
                            {
                            // InternalModel.g:1924:7: (lv_isReal_1_0= '.' )
                            // InternalModel.g:1925:8: lv_isReal_1_0= '.'
                            {
                            lv_isReal_1_0=(Token)match(input,48,FOLLOW_28); 

                            								newLeafNode(lv_isReal_1_0, grammarAccess.getNumExpressionAccess().getIsRealFullStopKeyword_0_0_1_0_0());
                            							

                            								if (current==null) {
                            									current = createModelElement(grammarAccess.getNumExpressionRule());
                            								}
                            								setWithLastConsumed(current, "isReal", true, ".");
                            							

                            }


                            }

                            // InternalModel.g:1937:6: ( (lv_decimalPart_2_0= RULE_INT ) )?
                            int alt20=2;
                            int LA20_0 = input.LA(1);

                            if ( (LA20_0==RULE_INT) ) {
                                alt20=1;
                            }
                            switch (alt20) {
                                case 1 :
                                    // InternalModel.g:1938:7: (lv_decimalPart_2_0= RULE_INT )
                                    {
                                    // InternalModel.g:1938:7: (lv_decimalPart_2_0= RULE_INT )
                                    // InternalModel.g:1939:8: lv_decimalPart_2_0= RULE_INT
                                    {
                                    lv_decimalPart_2_0=(Token)match(input,RULE_INT,FOLLOW_29); 

                                    								newLeafNode(lv_decimalPart_2_0, grammarAccess.getNumExpressionAccess().getDecimalPartINTTerminalRuleCall_0_0_1_1_0());
                                    							

                                    								if (current==null) {
                                    									current = createModelElement(grammarAccess.getNumExpressionRule());
                                    								}
                                    								setWithLastConsumed(
                                    									current,
                                    									"decimalPart",
                                    									lv_decimalPart_2_0,
                                    									"org.eclipse.xtext.common.Terminals.INT");
                                    							

                                    }


                                    }
                                    break;

                            }


                            }
                            break;

                    }


                    }


                    }
                    break;
                case 2 :
                    // InternalModel.g:1958:4: ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) )
                    {
                    // InternalModel.g:1958:4: ( ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) ) )
                    // InternalModel.g:1959:5: ( (lv_isReal_3_0= '.' ) ) ( (lv_decimalPart_4_0= RULE_INT ) )
                    {
                    // InternalModel.g:1959:5: ( (lv_isReal_3_0= '.' ) )
                    // InternalModel.g:1960:6: (lv_isReal_3_0= '.' )
                    {
                    // InternalModel.g:1960:6: (lv_isReal_3_0= '.' )
                    // InternalModel.g:1961:7: lv_isReal_3_0= '.'
                    {
                    lv_isReal_3_0=(Token)match(input,48,FOLLOW_30); 

                    							newLeafNode(lv_isReal_3_0, grammarAccess.getNumExpressionAccess().getIsRealFullStopKeyword_0_1_0_0());
                    						

                    							if (current==null) {
                    								current = createModelElement(grammarAccess.getNumExpressionRule());
                    							}
                    							setWithLastConsumed(current, "isReal", true, ".");
                    						

                    }


                    }

                    // InternalModel.g:1973:5: ( (lv_decimalPart_4_0= RULE_INT ) )
                    // InternalModel.g:1974:6: (lv_decimalPart_4_0= RULE_INT )
                    {
                    // InternalModel.g:1974:6: (lv_decimalPart_4_0= RULE_INT )
                    // InternalModel.g:1975:7: lv_decimalPart_4_0= RULE_INT
                    {
                    lv_decimalPart_4_0=(Token)match(input,RULE_INT,FOLLOW_29); 

                    							newLeafNode(lv_decimalPart_4_0, grammarAccess.getNumExpressionAccess().getDecimalPartINTTerminalRuleCall_0_1_1_0());
                    						

                    							if (current==null) {
                    								current = createModelElement(grammarAccess.getNumExpressionRule());
                    							}
                    							setWithLastConsumed(
                    								current,
                    								"decimalPart",
                    								lv_decimalPart_4_0,
                    								"org.eclipse.xtext.common.Terminals.INT");
                    						

                    }


                    }


                    }


                    }
                    break;

            }

            // InternalModel.g:1993:3: (otherlv_5= 'E' ( (lv_exponent_6_0= RULE_INT ) ) )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==49) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // InternalModel.g:1994:4: otherlv_5= 'E' ( (lv_exponent_6_0= RULE_INT ) )
                    {
                    otherlv_5=(Token)match(input,49,FOLLOW_30); 

                    				newLeafNode(otherlv_5, grammarAccess.getNumExpressionAccess().getEKeyword_1_0());
                    			
                    // InternalModel.g:1998:4: ( (lv_exponent_6_0= RULE_INT ) )
                    // InternalModel.g:1999:5: (lv_exponent_6_0= RULE_INT )
                    {
                    // InternalModel.g:1999:5: (lv_exponent_6_0= RULE_INT )
                    // InternalModel.g:2000:6: lv_exponent_6_0= RULE_INT
                    {
                    lv_exponent_6_0=(Token)match(input,RULE_INT,FOLLOW_2); 

                    						newLeafNode(lv_exponent_6_0, grammarAccess.getNumExpressionAccess().getExponentINTTerminalRuleCall_1_1_0());
                    					

                    						if (current==null) {
                    							current = createModelElement(grammarAccess.getNumExpressionRule());
                    						}
                    						setWithLastConsumed(
                    							current,
                    							"exponent",
                    							lv_exponent_6_0,
                    							"org.eclipse.xtext.common.Terminals.INT");
                    					

                    }


                    }


                    }
                    break;

            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleNumExpression"

    // Delegated rules


 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x000000000007C802L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000002010L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x000193F800000030L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0x0000000000081000L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000200010L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0x0000000000800002L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0x0000000001000002L});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x000000007E000002L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000000380000002L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0x0000000C00000002L});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x00000C0000000000L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x0000280000000000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0003000000000002L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0x0002000000000022L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0002000000000002L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000000000000020L});

}