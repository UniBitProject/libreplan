/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.navalplanner.web.externalcompanies;

import java.util.List;

import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link ExternalCompany}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ExternalCompanyModel implements IExternalCompanyModel {

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private ExternalCompany externalCompany;

    @Override
    public List<ExternalCompany> getCompanies() {
        return externalCompanyDAO.list(ExternalCompany.class);
    }

    @Override
    public ExternalCompany getCompany() {
        return externalCompany;
    }

    @Override
    public void initCreate() {
        externalCompany = ExternalCompany.create();
    }

    @Override
    @Transactional
    public void confirmSave() {
        externalCompanyDAO.save(externalCompany);
    }

}
