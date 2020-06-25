import React from "react";

import Select from 'react-select';

import "./adminka.css";
import NavbarHeader from "../competition-history/navbar-header/navbar-header";
import DefaultTextInput from "../common/default-text-input";
import DefaultSubmitButton from "../common/default-submit-button";


class UsersCollection extends React.Component {
    constructor(props) {
        super(props);
    }

    handleRoleChange = ({email, role}) => {
        console.log({email, role});
        this.props.handleRoleChange({email, role});
    };

    render() {
        const items = this.props.items;

        return (
            <div className={"user-grid user-grid-padding"}>
                {items.map(el => {
                    return (
                        <UserGridItem item={el} key={el.email} handleRoleChange={this.handleRoleChange}/>
                    )
                })}
            </div>
        )
    }
}


class UserGridItem extends React.Component {
    constructor(props) {
        super(props);

        this.options = [
            {value: "ROLE_STUDENT", label: "Студент"},
            {value: "ROLE_TEACHER", label: "Преподаватель"}
        ];

        this.state = {
            selectedOption: this.options.filter(option => option.value === props.item.role)
        };
    }

    handleRoleChange = (selectedOption) => {
        this.setState(
            { selectedOption, newRole: selectedOption.value },
            () => {
                console.log(`Option selected:`, this.state.selectedOption);
                this.props.handleRoleChange({email: this.props.item.email, role: selectedOption.value});
            }
        );
    };

    render() {
        const {item} = this.props;
        const {selectedOption} = this.state;
        console.log({state: this.state});

        const selectOptions = {
            value: selectedOption,
            onChange: this.handleRoleChange,
            options: this.options,
            styles: {
                container: (provided, state) => {
                    // none of react-select's styles are passed to <Control />
                    return {...provided, width: "100%", fontSize: "1em", marginLeft: 20};
                },
                valueContainer: (provided, _) => {
                    return {...provided, padding: 0, margin: 0, textAlign: "right"}
                },
                singleValue: (provided, state) => {
                    return { ...provided, right: 0};
                },
            }
        };

        const MySelect = props => (
            <Select
                {...props}
            />
        );

        return (
            <div className={"grid-item"}>
                <div className={"row justify-content-between"}>
                    <div style={{textAlign: "left"}}>
                        email:
                    </div>
                    <div style={{textAlign: "right"}}>
                        {item.email}
                    </div>
                </div>
                <div className={"row justify-content-between"} style={{paddingTop: "10px"}}>
                    <div style={{textAlign: "left"}} className={"grid-item-line"}>
                        Роль:
                    </div>
                    <div className={"col"} style={{paddingLeft: 0}}>
                    <MySelect {...selectOptions}/>
                    </div>
                </div>
            </div>
        )
    }
}

class AdminkaComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            items: [
                {email: "iivanov@hse.ru", role: "ROLE_TEACHER"},
                {email: "kpbenua@edu.hse.ru", role: "ROLE_STUDENT"},
                {email: "sdfomin1@edu.hse.ru", role: "ROLE_STUDENT"},
                {email: "sdfomin2@edu.hse.ru", role: "ROLE_STUDENT"},
                {email: "sdfomin3@edu.hse.ru", role: "ROLE_STUDENT"},
                {email: "sdfomin4@edu.hse.ru", role: "ROLE_STUDENT"},
                {email: "sdfomin5@edu.hse.ru", role: "ROLE_STUDENT"},
                {email: "sdfomin6@edu.hse.ru", role: "ROLE_STUDENT"},
            ],
            isShowingChanges: false
        };
        this.searchString = "";
        this.roleChanges = {};
    }

    handleRoleChange = ({email, role}) => {
        this.roleChanges[email] = role;
        console.log({roleChanges: this.roleChanges});

        if (this.state.items.filter(el => el.email === email)[0].role === role) {
            delete this.roleChanges[email];
        }
    };

    // call after fetching user roles from server, for showing already modified roles
    processServerUsers = (userList) => {
        return userList.map(el => {
            let {email} = el;
            if (email in this.roleChanges) {
                return {email, role: this.roleChanges[email]};
            }
            return el;
        })
    };

    onDoSearch = () => {
        console.log(`Request with search string: ${this.searchString}`);
        //request
        // TODO apply changes from roleChanges to fetched items
        // TODO after request set `isShowingChanges` to false
    };

    onSaveButtonClick = () => {
        //TODO
        // Promise.all(Object.keys(this.roleChanges).map(el => {
        //     return ApiHelper.changeRole(el, this.roleChanges[el])
        // })).catch(err => {
        //     console.log({err});
        //     showNotification(this).error(err.toString(), "Error", 3000);
        // }).then(_ => {
        //     showNotification(this).success("Роли успешно изменены", 3000);
        // });
    };

    onShowOrHideChanges = () => {
        this.setState(prevState => {
            return {isShowingChanges: !prevState.isShowingChanges};
        })
    };

    getChangesAsArray = () => {
        let arr = [];
        console.log(Object.entries(this.roleChanges));
        for (const [key, value] of Object.entries(this.roleChanges)) {
            arr.push({email: key, role: value});
        }
        console.log({arr});
        return arr;
    };

    render() {
        const inputStyle = {
            width: "100%",
            margin: "0",
            borderRadius: "15px",
            textAlign: "center"
        };

        const buttonStyle = {
            backgroundColor: "#48BDFF",
            borderWidth: "0",
            height: "100%",
            lineHeight: "1.5"
        };

        const items = this.state.isShowingChanges ? this.getChangesAsArray() : this.state.items;

        return (
            <div>
                <div>
                    <NavbarHeader/>
                </div>
                <div style={{paddingTop: "100px"}}>
                    <div>
                        <div>
                        <div style={{textAlign: "center", fontSize: "1.25rem"}}>Найти пользователя</div>
                        </div>
                        <div className={"row justify-content-end"} style={{paddingTop: "10px"}}>
                            <div className={"col-3"}>
                                <DefaultSubmitButton text={this.state.isShowingChanges ? "Скрыть изменения" : "Показать изменения"}
                                                     style={buttonStyle} onClick={this.onShowOrHideChanges}/>
                            </div>
                            <div className={"col-6"}>
                                <DefaultTextInput
                                    placeholder={"Имя, фамилия или email"}
                                    style={inputStyle}
                                    onChange={(text) => this.searchString = text}
                                    onKeyDown={(event) => {
                                        if (event.key === 'Enter') {
                                            event.preventDefault();
                                            event.stopPropagation();
                                            this.onDoSearch();
                                        }
                                }}
                                />
                            </div>
                            <div className={"col-3"} style={{marginBottom: "-10px"}}>
                                <DefaultSubmitButton text={"Сохранить изменения"} style={buttonStyle} onClick={this.onSaveButtonClick}/>
                            </div>

                        </div>
                    </div>
                    <div style={{paddingTop: "30px"}}>
                        <UsersCollection handleRoleChange={this.handleRoleChange} items={items}/>
                    </div>
                </div>
            </div>
        )
    }
}

export default AdminkaComponent;
