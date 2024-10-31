package org.test;


import com.evam.sdk.outputaction.AbstractOutputAction;
import com.evam.sdk.outputaction.IOMParameter;
import com.evam.sdk.outputaction.OutputActionContext;
import com.evam.sdk.outputaction.model.DesignerMetaParameters;
import com.evam.sdk.outputaction.model.ReturnParameter;

import java.util.List;

public class TestOA extends AbstractOutputAction {


    @Override
    public int execute(OutputActionContext outputActionContext) throws Exception {
        return 0;
    }

    @Override
    protected List<IOMParameter> getParameters() {
        return List.of();
    }

    @Override
    public boolean actionInputStringShouldBeEvaluated() {
        return false;
    }

    @Override
    public String getVersion() {
        return "";
    }

    @Override
    public boolean isReturnable() {
        return true;
    }

    @Override
    public ReturnParameter[] getRetParams(DesignerMetaParameters designerMetaParameters) {
        return super.getRetParams(designerMetaParameters);
    }
}
